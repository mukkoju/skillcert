import type { RazorpayOrder } from './api'

type CheckoutResponse = { razorpay_payment_id: string; razorpay_order_id: string; razorpay_signature: string }
type RazorpayInstance = { open: () => void }
type RazorpayConstructor = new (options: Record<string, unknown>) => RazorpayInstance

declare global { interface Window { Razorpay?: RazorpayConstructor } }

export function loadRazorpayCheckout(): Promise<RazorpayConstructor> {
  if (window.Razorpay) return Promise.resolve(window.Razorpay)
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.onload = () => window.Razorpay ? resolve(window.Razorpay) : reject(new Error('Razorpay Checkout was unavailable.'))
    script.onerror = () => reject(new Error('Unable to load Razorpay Checkout.'))
    document.head.append(script)
  })
}

export async function openRazorpayCheckout(order: RazorpayOrder, onSuccess: (response: CheckoutResponse) => Promise<void>, onDismiss: () => void) {
  const Razorpay = await loadRazorpayCheckout()
  const checkout = new Razorpay({
    key: order.keyId,
    amount: order.amountPaise,
    currency: order.currency,
    name: 'SkillCert by VAIONYX',
    description: `${order.courseName} credential`,
    order_id: order.razorpayOrderId,
    prefill: { name: order.recipientName },
    notes: { attempt_id: order.attemptId },
    theme: { color: '#126de8' },
    handler: onSuccess,
    modal: { ondismiss: onDismiss }
  })
  checkout.open()
}
