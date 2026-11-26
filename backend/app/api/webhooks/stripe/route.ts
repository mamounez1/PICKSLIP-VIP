import { NextRequest, NextResponse } from 'next/server';
import Stripe from 'stripe';
import { requireEnv } from '@/lib/env';
import { supabaseAdmin } from '@/lib/supabaseAdmin';

export const dynamic = 'force-dynamic';

function mapStatus(s: Stripe.Subscription.Status): 'active' | 'past_due' | 'canceled' {
  if (s === 'active' || s === 'trialing') return 'active';
  if (s === 'past_due' || s === 'unpaid') return 'past_due';
  return 'canceled';
}

export async function POST(req: NextRequest) {
  const stripe = new Stripe(requireEnv('STRIPE_SECRET_KEY'));
  const body = await req.text(); // corps brut obligatoire pour verifier la signature
  const sig = req.headers.get('stripe-signature') ?? '';

  let event: Stripe.Event;
  try {
    event = stripe.webhooks.constructEvent(body, sig, requireEnv('STRIPE_WEBHOOK_SECRET'));
  } catch (err: any) {
    return NextResponse.json({ error: `Signature invalide: ${err.message}` }, { status: 400 });
  }

  const db = supabaseAdmin();

  try {
    switch (event.type) {
      case 'checkout.session.completed': {
        const session = event.data.object as Stripe.Checkout.Session;
        const email = (session.customer_email ?? session.metadata?.subscriber_email ?? '').trim().toLowerCase();
        if (!email || !session.subscription) break;

        const sub = await stripe.subscriptions.retrieve(session.subscription as string);
        const { error } = await db.from('subscribers').upsert(
          {
            email,
            stripe_customer_id: session.customer as string,
            stripe_subscription_id: sub.id,
            subscription_status: mapStatus(sub.status),
            current_period_end: new Date(sub.current_period_end * 1000).toISOString(),
          },
          { onConflict: 'email' }
        );
        if (error) throw new Error(error.message);
        break;
      }

      case 'customer.subscription.updated':
      case 'customer.subscription.deleted': {
        const sub = event.data.object as Stripe.Subscription;
        const { error } = await db
          .from('subscribers')
          .update({
            subscription_status: event.type === 'customer.subscription.deleted' ? 'canceled' : mapStatus(sub.status),
            current_period_end: new Date(sub.current_period_end * 1000).toISOString(),
          })
          .eq('stripe_subscription_id', sub.id);
        if (error) throw new Error(error.message);
        break;
      }

      case 'invoice.payment_failed': {
        const inv = event.data.object as Stripe.Invoice;
        if (inv.subscription) {
          await db.from('subscribers').update({ subscription_status: 'past_due' }).eq('stripe_subscription_id', inv.subscription as string);
        }
        break;
      }

      default:
        break;
    }
  } catch (err: any) {
    // 500 => Stripe reessaie automatiquement l'envoi
    return NextResponse.json({ error: err.message }, { status: 500 });
  }

  return NextResponse.json({ received: true });
}
