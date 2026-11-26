import { NextRequest, NextResponse } from 'next/server';
import Stripe from 'stripe';
import { requireEnv } from '@/lib/env';

export const dynamic = 'force-dynamic';

export async function POST(req: NextRequest) {
  try {
    const { email } = await req.json();
    const clean = String(email ?? '').trim().toLowerCase();
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(clean)) {
      return NextResponse.json({ error: 'Email invalide' }, { status: 400 });
    }

    const stripe = new Stripe(requireEnv('STRIPE_SECRET_KEY'));
    const siteUrl = requireEnv('NEXT_PUBLIC_SITE_URL');

    const session = await stripe.checkout.sessions.create({
      mode: 'subscription',
      customer_email: clean,
      line_items: [{ price: requireEnv('STRIPE_PRICE_ID'), quantity: 1 }],
      success_url: `${siteUrl}/success`,
      cancel_url: `${siteUrl}/`,
      metadata: { subscriber_email: clean },
    });

    return NextResponse.json({ sessionId: session.id, url: session.url });
  } catch (error: any) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
