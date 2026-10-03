export type AssessmentOption = { id: string; label: string }
export type AssessmentQuestion = { id: string; prompt: string; theory: string | null; topic: string; difficulty: string; askedByCompany: string | null; codeSnippet: string | null; options: AssessmentOption[] }
export type Assessment = { slug: string; title: string; questionCount: number; passingScore: number; questions: AssessmentQuestion[] }
export type AttemptResult = { attemptId: string; passed: boolean; score: number; total: number; passingScore: number; credentialStatus: string }
export type CertificationSummary = { slug: string; title: string; description: string; durationMinutes: number; questionCount: number; passingScore: number; pricePaise: number; category: string }
export type RazorpayOrder = { attemptId: string; razorpayOrderId: string; amountPaise: number; currency: string; keyId: string; recipientName: string; courseName: string }
export type CertificateDetails = { shortId: string; recipientName: string; certificationSlug: string; courseName: string; score: number; totalQuestions: number; issuedAt: string; status: string; verificationUrl: string; pngUrl: string; pdfUrl: string }

// Never let a production build fall back to localhost. On Android, a localhost
// request can trigger Chrome's "access other apps and services" permission.
const productionApiBase = 'https://skillcertapi.vaionyxsolutions.online/api/v1'

export const apiBase =
  import.meta.env.VITE_API_BASE_URL ??
  (import.meta.env.PROD ? productionApiBase : 'http://localhost:8080/api/v1')

export async function loadAssessment(slug: string): Promise<Assessment> {
  const response = await fetch(`${apiBase}/certifications/${slug}/assessment`)
  if (!response.ok) throw new Error('Unable to load this assessment.')
  return response.json()
}

export async function loadCertifications(): Promise<CertificationSummary[]> {
  const response = await fetch(`${apiBase}/certifications`)
  if (!response.ok) throw new Error('Unable to load assessments.')
  return response.json()
}

export async function submitAttempt(slug: string, recipientName: string, answers: { questionId: string; optionId: string }[]): Promise<AttemptResult> {
  const response = await fetch(`${apiBase}/certifications/${slug}/attempts`, { headers: { 'Content-Type': 'application/json' }, method: 'POST', body: JSON.stringify({ recipientName, answers }) })
  if (!response.ok) throw new Error('Unable to submit this assessment.')
  return response.json()
}

export async function createRazorpayOrder(attemptId: string): Promise<RazorpayOrder> {
  const response = await fetch(`${apiBase}/payments/razorpay/orders`, { headers: { 'Content-Type': 'application/json' }, method: 'POST', body: JSON.stringify({ attemptId }) })
  if (!response.ok) throw new Error('Unable to start secure payment.')
  return response.json()
}

export async function saveAttemptContact(attemptId: string, email: string, mobile: string): Promise<void> {
  const response = await fetch(`${apiBase}/certifications/attempts/${attemptId}/contact`, {
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
    body: JSON.stringify({ email, mobile }),
  })
  if (!response.ok) throw new Error('Unable to save your contact details. Please try again.')
}

export async function verifyRazorpayPayment(payload: { attemptId: string; razorpayPaymentId: string; razorpayOrderId: string; razorpaySignature: string }): Promise<CertificateDetails> {
  const response = await fetch(`${apiBase}/payments/razorpay/verify`, { headers: { 'Content-Type': 'application/json' }, method: 'POST', body: JSON.stringify(payload) })
  if (!response.ok) throw new Error('Payment verification is still pending. Please refresh in a moment.')
  return response.json()
}

export async function loadIssuedCertificate(shortId: string): Promise<CertificateDetails> {
  const response = await fetch(`${apiBase}/certificates/verify/${encodeURIComponent(shortId)}`)
  if (!response.ok) throw new Error('This certificate could not be verified.')
  return response.json()
}
