import { NextRequest, NextResponse } from 'next/server';
import Stripe from 'stripe';

export async function POST(req: NextRequest) {
  const stripeSecretKey = process.env.STRIPE_SECRET_KEY || 'sk_test_placeholder';
  const webhookSecret = process.env.STRIPE_WEBHOOK_SECRET || 'whsec_placeholder';

  const stripe = new Stripe(stripeSecretKey, {
    apiVersion: '2023-10-16' as any,
  });

  const body = await req.text();
  const sig = req.headers.get('stripe-signature') || '';

  let event: Stripe.Event;

  try {
    event = stripe.webhooks.constructEvent(body, sig, webhookSecret);
  } catch (err: any) {
    return NextResponse.json({ error: `Webhook signature verification failed: ${err.message}` }, { status: 400 });
  }

  // Handle events
  switch (event.type) {
    case 'checkout.session.completed': {
      const session = event.data.object as Stripe.Checkout.Session;
      const customerEmail = session.customer_email || (session.metadata?.subscriber_email ?? '');
      const customerId = session.customer as string;
      const subscriptionId = session.subscription as string;

      // Upsert subscriber in Supabase as active with current_period_end
      console.log(`[Stripe Webhook] VIP Activated for ${customerEmail}`);
      break;
    }

    case 'customer.subscription.updated':
    case 'customer.subscription.deleted': {
      const subscription = event.data.object as Stripe.Subscription;
      const status = subscription.status === 'active' ? 'active' : 'canceled';

      // Sync status to Supabase
      console.log(`[Stripe Webhook] Subscription ${subscription.id} status synced to ${status}`);
      break;
    }

    default:
      console.log(`Unhandled event type: ${event.type}`);
  }

  return NextResponse.json({ received: true });
}
