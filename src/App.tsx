import { AnimatePresence, motion } from 'framer-motion'
import { Award, BookOpen, Brain, Check, ChevronRight, Clock3, Cloud, Code2, Database, LoaderCircle, LockKeyhole, Mail, Megaphone, Palette, Search, ShieldCheck, Volume2, VolumeX, Workflow, X, Zap } from 'lucide-react'
import { useEffect, useRef, useState, type CSSProperties } from 'react'
import { apiBase, createRazorpayOrder, loadAssessment, loadCertifications, loadIssuedCertificate, saveAttemptContact, submitAttempt, verifyRazorpayPayment, type Assessment, type AssessmentQuestion, type AttemptResult, type CertificateDetails, type CertificationSummary } from './api'
import { initialiseMetaPixel, trackPixel } from './metaPixel'
import { openRazorpayCheckout } from './razorpay'

type Step = 'intro' | 'loading' | 'name' | 'preparing-companies' | 'preparing-stories' | 'quiz' | 'analysis' | 'contact' | 'result' | 'checkout' | 'payment-processing' | 'success' | 'failed'

const variants = { initial: { opacity: 0, y: 18 }, animate: { opacity: 1, y: 0 }, exit: { opacity: 0, y: -18 } }
const assetBaseUrl = (import.meta.env.VITE_ASSET_BASE_URL ?? (import.meta.env.PROD ? 'https://assets.vaionyxsolutions.online' : '')).replace(/\/$/, '')
const assetUrl = (path: string) => assetBaseUrl ? `${assetBaseUrl}${path}` : path

function Brand() { return <div className="brand"><img src={assetUrl('/images/skillcert-logo.webp')} alt="SkillCert by VAIONYX"/></div> }
function Button({ children, onClick, secondary = false }: { children: React.ReactNode, onClick?: () => void, secondary?: boolean }) { return <button className={`button ${secondary ? 'secondary' : ''}`} onClick={onClick}>{children}<ChevronRight size={22}/></button> }
function QuestionPreparationStatus({ courseName }: { courseName: string }) { return <div className="question-prep-status"><i/><span>Preparing short questions for <b>{courseName}</b></span></div> }
function QuestionTags({ question }: { question: AssessmentQuestion }) { return <div className="question-tags"><span>{question.topic}</span><span>{question.difficulty}</span>{question.askedByCompany ? <span>Asked by {question.askedByCompany}</span> : <span>Industry-style practice</span>}</div> }
function QuestionCode({ code }: { code: string | null }) { return code ? <pre className="question-code"><code>{code}</code></pre> : null }

function CertificateVerification({ shortId }: { shortId: string }) {
  const [certificate, setCertificate] = useState<CertificateDetails | null>(null)
  const [error, setError] = useState('')
  useEffect(() => {
    void loadIssuedCertificate(shortId).then(setCertificate).catch((reason: Error) => setError(reason.message))
  }, [shortId])
  if (error) return <motion.section {...variants} className="hero verification-page"><Brand/><div className="failure-mark">×</div><h1>Certificate <em>not found.</em></h1><p>{error}</p></motion.section>
  if (!certificate) return <motion.section {...variants} className="hero verification-page"><Brand/><div className="payment-processing-spinner"><LoaderCircle/></div><h1>Verifying <em>certificate.</em></h1><p>Please wait while we check the SkillCert record.</p></motion.section>
  const issuedDate = new Intl.DateTimeFormat('en-IN', { dateStyle: 'long' }).format(new Date(certificate.issuedAt))
  const apiOrigin = apiBase.replace(/\/api\/v1\/?$/, '')
  const fileUrl = (url: string) => url.startsWith('http') ? url : `${apiOrigin}${url}`
  return <motion.section {...variants} className="hero verification-page"><Brand/><div className="verified-heading"><ShieldCheck/><span>VERIFIED CERTIFICATE</span></div><h1>This certificate is <em>valid.</em></h1><p>The SkillCert record below is publicly verifiable.</p><div className="verified-certificate"><img src={fileUrl(certificate.pngUrl)} alt={`${certificate.courseName} certificate for ${certificate.recipientName}`}/></div><dl className="verification-details"><div><dt>Issued to</dt><dd>{certificate.recipientName}</dd></div><div><dt>Course</dt><dd>{certificate.courseName}</dd></div><div><dt>Certificate ID</dt><dd>{certificate.shortId}</dd></div><div><dt>Issued on</dt><dd>{issuedDate}</dd></div></dl><a className="button verification-download" href={fileUrl(certificate.pdfUrl)} target="_blank" rel="noreferrer">View certificate PDF <ChevronRight size={20}/></a></motion.section>
}

export default function App() {
  const [path, setPath] = useState(window.location.pathname)
  const [step, setStep] = useState<Step>('loading')
  const [name, setName] = useState('')
  const [question, setQuestion] = useState(0)
  const [score, setScore] = useState(0)
  const [assessment, setAssessment] = useState<Assessment | null>(null)
  const [answers, setAnswers] = useState<{ questionId: string; optionId: string }[]>([])
  const [attemptResult, setAttemptResult] = useState<AttemptResult | null>(null)
  const [issuedCertificate, setIssuedCertificate] = useState<CertificateDetails | null>(null)
  const [paymentError, setPaymentError] = useState<string | null>(null)
  const [openingCheckout, setOpeningCheckout] = useState(false)
  const [quizError, setQuizError] = useState<string | null>(null)
  const [assessmentLoadError, setAssessmentLoadError] = useState<string | null>(null)
  const [startCountdown, setStartCountdown] = useState(3)
  const [showQuizLaunch, setShowQuizLaunch] = useState(false)
  const [analysisCount, setAnalysisCount] = useState(0)
  const [musicMuted, setMusicMuted] = useState(false)
  const [resultHeadlineVariant] = useState(() => Math.random() > .5)
  const [catalogue, setCatalogue] = useState<CertificationSummary[]>([])
  const [theory, setTheory] = useState(false)
  const [email, setEmail] = useState('')
  const [mobile, setMobile] = useState('')
  const [formError, setFormError] = useState('')
  const routeSlug = path.match(/^\/([^/]+)$/)?.[1]
  const courseSlug = routeSlug && routeSlug !== 'assessments' ? routeSlug : undefined
  const audioContext = useRef<AudioContext | null>(null)
  const quizMusic = useRef<HTMLAudioElement | null>(null)
  const formScrollTimer = useRef<number | null>(null)
  const revealFormControl = (control: HTMLElement) => {
    // Mobile browsers resize at different points while their keyboard opens.
    // Wait for that resize, then keep the current field and its action visible.
    if (formScrollTimer.current) window.clearTimeout(formScrollTimer.current)
    window.setTimeout(() => control.scrollIntoView({ behavior: 'smooth', block: 'center' }), 320)
  }
  const restoreFormPosition = () => {
    if (formScrollTimer.current) window.clearTimeout(formScrollTimer.current)
    formScrollTimer.current = window.setTimeout(() => {
      if (document.activeElement instanceof HTMLInputElement) return
      document.querySelector('main')?.scrollTo({ top: 0, behavior: 'smooth' })
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }, 180)
  }
  useEffect(() => { initialiseMetaPixel() }, [])
  useEffect(() => {
    // Meta needs a PageView for each route because this is a single-page app.
    trackPixel('PageView')
  }, [path])
  const getAudioContext = () => {
    audioContext.current ??= new AudioContext()
    return audioContext.current
  }
  const navigate = (nextPath: string) => { window.history.pushState({}, '', nextPath); setAssessment(null); setAssessmentLoadError(null); setPath(nextPath); setStep('loading') }
  const startPreparation = () => {
    if (!name.trim()) { setFormError('Enter the name you want printed on your certificate.'); return }
    setName(name.trim())
    trackPixel('StartAssessment', { content_name: assessment?.title ?? courseSlug ?? 'SkillCert assessment' })
    setFormError('')
    try {
      void getAudioContext().resume()
    } catch { /* Audio is optional; the countdown still works. */ }
    // Create the track within the Continue click so browsers permit playback later.
    const music = quizMusic.current ?? new Audio(assetUrl('/audio/skillcert-exam-ambience.mp3'))
    music.loop = true
    music.volume = 0
    quizMusic.current = music
    void music.play().catch(() => undefined)
    // Refresh once more at the user action, so the quiz never begins without questions.
    void loadAssessment(courseSlug ?? 'cloud-foundations')
      .then(setAssessment)
      .catch((error: Error) => setQuizError(error.message))
    setShowQuizLaunch(true)
    setStep('preparing-companies')
  }
  const saveContactDetails = async () => {
    const mobileDigits = mobile.replace(/\D/g, '')
    if (!mobile.trim() || !email.trim()) { setFormError('Enter both your mobile number and email address.'); return }
    if (mobileDigits.length < 10) { setFormError('Enter a valid mobile number.'); return }
    if (!/^\S+@\S+\.\S+$/.test(email)) { setFormError('Enter a valid email address.'); return }
    if (!attemptResult?.attemptId) { setFormError('Your assessment could not be found. Please try again.'); return }
    try {
      await saveAttemptContact(attemptResult.attemptId, email.trim(), mobile.trim())
      trackPixel('Lead', { content_name: assessment?.title ?? 'SkillCert assessment' })
      setFormError('')
      setStep('result')
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Unable to save your contact details. Please try again.')
    }
  }
  const playExamTone = () => {
    const context = getAudioContext()
    void context.resume()
    const oscillator = context.createOscillator()
    const gain = context.createGain()
    oscillator.type = 'sine'
    oscillator.frequency.value = 660
    gain.gain.setValueAtTime(.07, context.currentTime)
    gain.gain.exponentialRampToValueAtTime(.001, context.currentTime + .14)
    oscillator.connect(gain).connect(context.destination)
    oscillator.start()
    oscillator.stop(context.currentTime + .14)
  }
  const playOptionTap = () => {
    const context = getAudioContext()
    void context.resume()
    const oscillator = context.createOscillator()
    const gain = context.createGain()
    oscillator.type = 'triangle'
    oscillator.frequency.setValueAtTime(620, context.currentTime)
    oscillator.frequency.exponentialRampToValueAtTime(430, context.currentTime + .08)
    gain.gain.setValueAtTime(.14, context.currentTime)
    gain.gain.exponentialRampToValueAtTime(.001, context.currentTime + .12)
    oscillator.connect(gain).connect(context.destination)
    oscillator.start()
    oscillator.stop(context.currentTime + .125)
  }
  const playQuestionCue = () => {
    const context = getAudioContext()
    void context.resume()
    const oscillator = context.createOscillator()
    const gain = context.createGain()
    oscillator.type = 'sine'
    oscillator.frequency.setValueAtTime(430, context.currentTime)
    oscillator.frequency.setValueAtTime(620, context.currentTime + .07)
    gain.gain.setValueAtTime(.1, context.currentTime)
    gain.gain.exponentialRampToValueAtTime(.001, context.currentTime + .22)
    oscillator.connect(gain).connect(context.destination)
    oscillator.start()
    oscillator.stop(context.currentTime + .23)
  }
  const toggleQuizMusic = () => {
    const music = quizMusic.current
    if (!music) return
    const nextMuted = !musicMuted
    setMusicMuted(nextMuted)
    music.muted = nextMuted
    if (!nextMuted) void music.play().catch(() => undefined)
  }

  useEffect(() => {
    if (step !== 'loading' || !courseSlug) return

    let cancelled = false
    setAssessmentLoadError(null)
    setQuizError(null)

    loadAssessment(courseSlug)
      .then((loadedAssessment) => {
        if (cancelled) return
        setAssessment(loadedAssessment)
        setStep('intro')
      })
      .catch((error: Error) => {
        if (!cancelled) setAssessmentLoadError(error.message)
      })

    return () => {
      cancelled = true
    }
  }, [courseSlug, path, step])
  useEffect(() => { loadCertifications().then(setCatalogue).catch(() => undefined) }, [])
  useEffect(() => {
    if (step !== 'intro' || assessment) return
    let cancelled = false
    loadAssessment(courseSlug ?? 'cloud-foundations').then(loaded => { if (!cancelled) setAssessment(loaded) }).catch(() => undefined)
    return () => { cancelled = true }
  }, [assessment, courseSlug, step])
  useEffect(() => {
    if (step !== 'intro' || !assessment) return
    trackPixel('ViewContent', { content_name: assessment.title, content_type: 'assessment' })
  }, [assessment, step])
  useEffect(() => {
    if (step !== 'preparing-companies' && step !== 'preparing-stories') return
    if (step === 'preparing-stories' && !assessment) return
    const nextStep = step === 'preparing-companies' ? 'preparing-stories' : 'quiz'
    const timer = window.setTimeout(() => setStep(nextStep), 2000)
    return () => window.clearTimeout(timer)
  }, [assessment, step])
  useEffect(() => {
    if (step !== 'quiz' || assessment) return
    let cancelled = false
    setQuizError(null)
    loadAssessment(courseSlug ?? 'cloud-foundations')
      .then(loadedAssessment => { if (!cancelled) setAssessment(loadedAssessment) })
      .catch((error: Error) => { if (!cancelled) setQuizError(error.message) })
    return () => { cancelled = true }
  }, [assessment, courseSlug, step])
  useEffect(() => {
    if (step !== 'quiz' || !showQuizLaunch) return
    setStartCountdown(3)
    playExamTone()
    let timer = 0
    timer = window.setInterval(() => {
      setStartCountdown(current => {
        if (current <= 1) {
          window.clearInterval(timer)
          window.setTimeout(() => setShowQuizLaunch(false), 160)
          return 0
        }
        playExamTone()
        return current - 1
      })
    }, 1000)
    return () => window.clearInterval(timer)
  }, [showQuizLaunch, step])
  useEffect(() => {
    if (step !== 'quiz' || showQuizLaunch || !assessment) return
    const music = quizMusic.current ?? new Audio(assetUrl('/audio/skillcert-exam-ambience.mp3'))
    music.loop = true
    quizMusic.current = music
    music.muted = musicMuted
    music.volume = musicMuted ? 0 : .38
    void music.play().catch(() => undefined)
    return () => {
      music.pause()
      music.currentTime = 0
      music.volume = 0
    }
  }, [assessment, showQuizLaunch, step])
  useEffect(() => {
    const music = quizMusic.current
    if (!music) return
    music.muted = musicMuted
    music.volume = musicMuted ? 0 : .38
  }, [musicMuted])
  useEffect(() => {
    if (step === 'quiz' && !showQuizLaunch && assessment) playQuestionCue()
  }, [assessment, question, showQuizLaunch, step])
  const choose = async (optionId: string) => {
    if (!assessment) return
    playOptionTap()
    const nextAnswers = [...answers, { questionId: assessment.questions[question].id, optionId }]
    setAnswers(nextAnswers)
    if (question < assessment.questions.length - 1) { setTimeout(() => setQuestion(q => q + 1), 280); return }
    // Show the analysis experience instantly; scoring continues in the background.
    setAttemptResult(null)
    setStep('analysis')
    try {
      const result = await submitAttempt(assessment.slug, name, nextAnswers)
      setAttemptResult(result)
      trackPixel('CompleteRegistration', { content_name: assessment.title, status: result.passed ? 'passed' : 'not_passed' })
    } catch (error) { setQuizError(error instanceof Error ? error.message : 'Unable to submit this assessment.'); setStep('quiz') }
  }
  useEffect(() => {
    if (step !== 'analysis') return
    const total = assessment?.questions.length ?? 5
    setAnalysisCount(0)
    let completed = 0
    const progressTimer = window.setInterval(() => {
      completed += 1
      setAnalysisCount(completed)
      if (completed >= total) window.clearInterval(progressTimer)
    }, 260)
    return () => { window.clearInterval(progressTimer) }
  }, [assessment, step])
  useEffect(() => {
    if (step !== 'analysis' || !attemptResult) return
    const timer = window.setTimeout(() => setStep('contact'), 3000)
    return () => window.clearTimeout(timer)
  }, [attemptResult, step])
  const pricePaise = catalogue.find(item => item.slug === assessment?.slug)?.pricePaise ?? 19900
  const price = `₹${(pricePaise / 100).toLocaleString('en-IN')}`
  const beginCheckout = async () => {
    if (openingCheckout) return
    if (!attemptResult?.attemptId) { setPaymentError('Complete and pass the assessment before issuing a credential.'); return }
    try {
      setPaymentError(null)
      setOpeningCheckout(true)
      trackPixel('InitiateCheckout', { content_name: assessment?.title ?? 'SkillCert assessment', value: pricePaise / 100, currency: 'INR' })
      const order = await createRazorpayOrder(attemptResult.attemptId)
      await openRazorpayCheckout(order, async response => {
        setOpeningCheckout(false)
        setStep('payment-processing')
        try {
          const certificate = await verifyRazorpayPayment({ attemptId: order.attemptId, razorpayPaymentId: response.razorpay_payment_id, razorpayOrderId: response.razorpay_order_id, razorpaySignature: response.razorpay_signature })
          setIssuedCertificate(certificate)
          trackPixel('Purchase', { content_name: certificate.courseName, value: pricePaise / 100, currency: 'INR' })
          setStep('success')
        } catch (error) { setPaymentError(error instanceof Error ? error.message : 'Unable to verify payment.'); setStep('failed') }
      }, () => setOpeningCheckout(false))
    } catch (error) { setOpeningCheckout(false); setPaymentError(error instanceof Error ? error.message : 'Unable to open secure payment.'); setStep('failed') }
  }

  const content: Record<Step, React.ReactNode> = {
    intro: <CourseIntro courseName={assessment?.title ?? 'Cloud Foundations'} onStart={() => setStep('name')}/>,
    loading: <motion.section {...variants} className="hero assessment-loader"><Brand/>{assessmentLoadError ? <div className="assessment-loader-error"><p>We couldn’t load this assessment.</p><button className="link" onClick={() => window.location.reload()}>Try again</button></div> : <div className="assessment-loader-status" aria-live="polite"><span className="assessment-loader-dots" aria-hidden="true"><i/><i/><i/></span><small>Just a moment</small></div>}</motion.section>,
    name: <motion.section {...variants} className="hero name"><Brand/><div className="eyebrow">LEARN · ASSESS · GET CERTIFIED</div><h1>Who is this <em>certificate</em> for?</h1><p>Use the name you want shown on your certificate.</p><label className="input"><Award size={22}/><input value={name} maxLength={30} placeholder="Your Full name (Ex: Rahul Singh)" onFocus={e => revealFormControl(e.currentTarget)} onBlur={restoreFormPosition} onChange={e => { setName(e.target.value); setFormError('') }} aria-label="Name on certificate" autoComplete="name"/></label>{formError && <small className="form-error">{formError}</small>}<Button onClick={startPreparation}>Continue</Button><div className="achievement-promise"><strong>Your certificate can help you <em>stand out to 500+ companies, including</em></strong><div className="career-logos"><span className="google-mark"><i>G</i>Google</span><span className="microsoft-mark"><i><b/><b/><b/><b/></i>Microsoft</span><span className="amazon-mark"><i>a</i>amazon</span><span className="meta-mark"><i>∞</i>Meta</span><span className="adobe-mark"><i>A</i>Adobe</span></div></div></motion.section>,
    'preparing-companies': <motion.section {...variants} className="hero preparation-screen"><Brand/><QuestionPreparationStatus courseName={assessment?.title ?? 'Cloud Foundations'}/><div className="prep-copy"><div className="eyebrow">YOUR SKILL, MADE VISIBLE</div><h1>Carry your proof into your <em>next opportunity.</em></h1><p>A SkillCert certificate is designed to be simple to share and easy to verify.</p></div><div className="prep-company-grid"><span className="google-mark"><i>G</i>Google</span><span className="microsoft-mark"><i><b/><b/><b/><b/></i>Microsoft</span><span className="amazon-mark"><i>a</i>amazon</span><span className="meta-mark"><i>∞</i>Meta</span><span className="adobe-mark"><i>A</i>Adobe</span><span className="more-mark">+500<br/><small>companies</small></span></div><small className="prep-footnote">Your questions are being selected now.</small></motion.section>,
    'preparing-stories': <motion.section {...variants} className="hero preparation-screen"><Brand/><QuestionPreparationStatus courseName={assessment?.title ?? 'Cloud Foundations'}/><div className="prep-copy"><div className="eyebrow">CAREER MOMENTUM STARTS SMALL</div><h1>One assessment. A stronger <em>next step.</em></h1><p>Focused practice today can make you more confident for tomorrow's roles.</p></div><div className="prep-profile-grid"><article><img src={assetUrl('/images/avatar-ananya-sharma.webp')} alt="Fictional profile avatar for Ananya Sharma"/><div><strong>Ananya Sharma</strong><small>Cloud Support Associate</small><span>₹6–9 LPA potential</span></div></article><article><img src={assetUrl('/images/avatar-arjun-reddy.webp')} alt="Fictional profile avatar for Arjun Reddy"/><div><strong>Arjun Reddy</strong><small>Junior Cloud Engineer</small><span>₹8–12 LPA potential</span></div></article><article><img src={assetUrl('/images/avatar-kavya-nair.webp')} alt="Fictional profile avatar for Kavya Nair"/><div><strong>Kavya Nair</strong><small>Platform Analyst</small><span>₹10–16 LPA potential</span></div></article></div><small className="prep-footnote">Illustrative career snapshots — not salary promises.</small></motion.section>,
    quiz: <motion.section {...variants} className="hero quiz">{showQuizLaunch ? <div className="quiz-launch"><Brand/><QuestionPreparationStatus courseName={assessment?.title ?? 'Cloud Foundations'}/><div><h1>Get set for your <em>assessment.</em></h1><p>Your first question starts in a moment.</p></div><button className="button countdown-button" disabled>Get Started in {startCountdown || '…'}</button><div className="countdown-progress"><i style={{ width: `${(startCountdown / 3) * 100}%` }}/></div><small>Starting automatically</small></div> : assessment ? <><div className="quiz-head"><Brand/><button className="icon" onClick={toggleQuizMusic} aria-label={musicMuted ? 'Turn quiz music on' : 'Mute quiz music'}>{musicMuted ? <VolumeX/> : <Volume2/>}</button></div><div className="progress-label">Question {question + 1} of {assessment.questions.length}</div><div className="progress">{assessment.questions.map((_, i) => <i key={i} className={i <= question ? 'active' : ''}/>)}</div><QuestionTags question={assessment.questions[question]}/><QuestionCode code={assessment.questions[question].codeSnippet}/><h2>{assessment.questions[question].prompt}</h2><div className="choices">{assessment.questions[question].options.map(option => <button key={option.id} onClick={() => choose(option.id)}><i/>{option.label}</button>)}</div><div className="quiz-footer"><button className="theory-button" onClick={() => setTheory(true)}><BookOpen/> Read concept</button><span>Select an answer to continue</span></div>{theory && <Theory close={() => setTheory(false)} text={assessment.questions[question].theory}/>}</> : <><Brand/><h1>Preparing your <em>questions</em></h1><p>{quizError ?? 'Reconnecting to the assessment service.'}</p></>}</motion.section>,
    analysis: <motion.section {...variants} className="hero analysis"><Brand/><div className="analysis-copy"><h1>Analysing your <em>results.</em></h1><p>Checking each response against the assessment standard.</p></div><div className="analysis-ring" style={{ '--analysis-progress': `${(analysisCount / (assessment?.questions.length ?? 5)) * 360}deg` } as CSSProperties}><div><b>{analysisCount}</b><span>of {assessment?.questions.length ?? 5}</span><small>QUESTIONS COMPLETED</small></div></div><div className="analysis-insights"><article className="speed-insight"><i><Zap/></i><b>You are faster than</b><span>60% people</span></article><article><b>{assessment?.questions.length ?? 5} topics</b><span>Answers are ready for review.</span></article></div><small className="analysis-status">Preparing your certificate journey…</small></motion.section>,
    contact: <motion.section {...variants} className="hero contact"><Brand/><h1>Where should we <em>send your certificate?</em></h1><p>We will use these details only for certificate delivery and support.</p><label className="input"><Volume2 size={22}/><input value={mobile} placeholder="Mobile number (Ex: +91 98765 43210)" onFocus={e => revealFormControl(e.currentTarget)} onBlur={restoreFormPosition} onChange={e => { setMobile(e.target.value); setFormError('') }} aria-label="WhatsApp number" inputMode="tel" autoComplete="tel"/></label><label className="input"><Mail size={22}/><input value={email} placeholder="Email address (Ex: rahul@example.com)" onFocus={e => revealFormControl(e.currentTarget)} onBlur={restoreFormPosition} onChange={e => { setEmail(e.target.value); setFormError('') }} aria-label="Email address" inputMode="email" autoComplete="email"/></label>{formError && <small className="form-error">{formError}</small>}<small className="privacy"><LockKeyhole/> Private and used only for this assessment.</small><Button onClick={saveContactDetails}>Save & view result</Button></motion.section>,
    result: <motion.section {...variants} className="hero result offer-result"><Brand/>{attemptResult?.passed ? <><h1>{resultHeadlineVariant ? <>Awesome {name.split(' ')[0] || 'there'}! <em>Test completed!</em></> : <>Congrats {name.split(' ')[0] || 'there'}! You scored a <em>great {Math.round(((attemptResult?.score ?? 0) / (attemptResult?.total ?? 5)) * 100)}%!</em></>}</h1><p className="offer-subtitle">Unlock instant certificate to view score &amp; prove you’re in the <em>top 18%</em> of <strong>{assessment?.title ?? 'this course'}</strong>.</p><div className="offer-certificate"><img src={assetUrl('/images/skillcert-certificate-template.webp')} alt="Preview of your SkillCert certificate"/><strong className="offer-recipient">Your Name</strong><strong className="offer-course">Course Name</strong><div className="certificate-recognition"><b>This certificate is recognized by 1100+ companies like</b><div><span className="google-mark"><i>G</i>Google</span><span className="microsoft-mark"><i><b/><b/><b/><b/></i>Microsoft</span><span className="amazon-mark"><i>a</i>amazon</span><span className="meta-mark"><i>∞</i>Meta</span></div></div></div><p className="hired-claim"><b>8/10</b> CVs with this certificate got hired</p><div className="offer-sticky"><button className="button certificate-cta" onClick={beginCheckout} disabled={openingCheckout}>{openingCheckout ? <><LoaderCircle className="button-spinner"/> Opening secure payment…</> : <>Get my certificate <ChevronRight size={22}/></>}</button><p className="offer-price">For just: <b>{price}</b> <s>₹599</s></p></div></> : <><h1>Keep building your <em>foundation.</em></h1><p>{assessment?.title ?? 'This assessment'} can be attempted again when you’re ready.</p><Button onClick={() => { setQuestion(0); setAnswers([]); setStep('quiz') }}>Try again</Button></>}</motion.section>,
    checkout: <motion.section {...variants} className="hero checkout"><Brand/><h1>Complete <em>secure payment</em></h1><p>Unlock your verified credential with a safe, seamless payment.</p><div className="order"><span>Verified Skill Credential</span><strong>Cloud Foundations</strong><small>Recipient: {name} <b>₹18</b></small></div><div className="methods"><button className="selected">UPI</button><button>Card</button><button>Net banking</button></div><div className="payment-qr">▦<span>Pay with any UPI app</span></div><Button onClick={() => setStep('success')}>Pay ₹18 securely</Button><small><LockKeyhole size={14}/> Payments are processed securely by Razorpay</small><button className="demo-fail" onClick={() => setStep('failed')}>Demo: payment failed state</button></motion.section>,
    'payment-processing': <motion.section {...variants} className="hero payment-processing"><Brand/><div className="payment-processing-spinner"><LoaderCircle/></div><h1>Issuing your <em>certificate.</em></h1><p>Payment received. We’re securely verifying it and creating your QR-verifiable certificate now.</p><small>Please don’t close this page.</small></motion.section>,
    success: <motion.section {...variants} className="hero success"><Brand/><div className="success-mark"><Check/></div><h1>Your certificate <em>is ready.</em></h1><p>A verified {issuedCertificate?.courseName ?? assessment?.title ?? 'SkillCert'} certificate has been issued for {name} and sent to <b>{email}</b>.</p><button className="button success-explore" onClick={() => { window.history.pushState({}, '', '/assessments'); setPath('/assessments') }}>Explore more certifications <ChevronRight size={18}/></button></motion.section>,
    failed: <motion.section {...variants} className="hero failed"><Brand/><div className="failure-mark">×</div><h1>Payment <em>did not go through.</em></h1><p>{paymentError ?? 'No credential has been issued and no successful payment was confirmed.'}</p><div className="order">{assessment?.title ?? 'SkillCert'} credential <strong>{price}</strong></div><Button onClick={beginCheckout}>Try payment again</Button><button className="link" onClick={() => setStep('result')}>Return to result</button><small>Need help? Contact SkillCert support.</small></motion.section>
  }
  const verificationId = path.match(/^\/verify\/([^/]+)$/)?.[1]
  const page = verificationId ? <CertificateVerification shortId={verificationId}/> : courseSlug ? content[step] : path === '/assessments' ? <Explorer catalogue={catalogue} onSelect={(slug) => navigate(`/${slug}?src=catalogue`)} onHome={() => { window.history.pushState({}, '', '/'); setPath('/') }} /> : <Landing onStart={() => navigate('/cloud-foundations?src=direct')} onExplore={() => { window.history.pushState({}, '', '/assessments'); setPath('/assessments') }} />
  return <main><div className="ambient a"/><div className="ambient b"/><AnimatePresence mode="wait">{page}</AnimatePresence></main>
}

function SiteNav({ onExplore, onHome }: { onExplore?: () => void, onHome?: () => void }) { return <nav className="site-nav"><button className="logo-button" onClick={onHome}><Brand/></button><div><button onClick={onExplore}>Explore skills</button><button>How it works</button><button>Verify credential</button>{onExplore && <button className="nav-cta" onClick={onExplore}>Start assessment</button>}</div></nav> }

const companySets = [
  [
    { label: 'PhonePe', icon: assetUrl('/company-logos/phonepe-wordmark.svg') },
    { label: 'CRED', icon: assetUrl('/company-logos/cred-wordmark.svg') },
    { label: 'Zepto', icon: assetUrl('/company-logos/zepto-wordmark.svg') },
    { label: 'OYO', icon: assetUrl('/company-logos/oyo-wordmark.svg') },
    { label: 'Meesho', icon: assetUrl('/company-logos/meesho-wordmark.svg') },
  ],
  [
    { label: 'Dream11', icon: assetUrl('/company-logos/dream11-wordmark.svg') },
    { label: 'Rapido', icon: assetUrl('/company-logos/rapido-wordmark.svg') },
    { label: 'Flipkart', icon: assetUrl('/company-logos/flipkart-wordmark.svg') },
    { label: 'Amazon', icon: assetUrl('/company-logos/amazon-wordmark.svg') },
    { label: 'Microsoft', icon: assetUrl('/company-logos/microsoft-wordmark.svg') },
  ],
] as const

const testimonials = [
  { employer: 'PhonePe', logo: assetUrl('/company-logos/phonepe-wordmark.svg'), name: 'Ananya Sharma', location: 'Hyderabad', quote: <>“This certificate helped me land my <em>dream job at PhonePe.</em>”</> },
  { employer: 'CRED', logo: assetUrl('/company-logos/cred-wordmark.svg'), name: 'Rohan Mehta', location: 'Pune', quote: <>“It gave my profile the <em>proof CRED recruiters wanted.</em>”</> },
  { employer: 'Microsoft', logo: assetUrl('/company-logos/microsoft-wordmark.svg'), name: 'Kavya Nair', location: 'Bengaluru', quote: <>“I could share my verified skill <em>in one link.</em>”</> },
]

function CompanyMark({ mark }: { mark: typeof companySets[number][number] }) {
  return <img className="intro-company-logo" src={mark.icon} alt={mark.label}/>
}

function CourseIntro({ courseName, onStart }: { courseName: string, onStart: () => void }) {
  const [companySet, setCompanySet] = useState(0)
  const [testimonialIndex, setTestimonialIndex] = useState(0)
  const [activityIndex, setActivityIndex] = useState(0)
  useEffect(() => {
    const timer = window.setInterval(() => setCompanySet(current => (current + 1) % companySets.length), 3000)
    return () => window.clearInterval(timer)
  }, [])
  useEffect(() => {
    const timer = window.setInterval(() => setTestimonialIndex(current => (current + 1) % testimonials.length), 3300)
    return () => window.clearInterval(timer)
  }, [])
  useEffect(() => {
    const timer = window.setInterval(() => setActivityIndex(current => (current + 1) % 2), 2800)
    return () => window.clearInterval(timer)
  }, [])
  const testimonial = testimonials[testimonialIndex]
  return <motion.section {...variants} className="course-intro">
    <Brand/>
    <header className="course-intro-head">
      <h1>Get your <em>{courseName} Certificate</em></h1>
      <p>Made to help you <b>stand out.</b></p>
    </header>
    <div className="intro-company-proof">
      <span>Recognised by 500+ Top Companies</span>
      <div key={companySet} className="intro-company-row">
        {companySets[companySet].map(mark => <CompanyMark key={mark.label} mark={mark}/>) }
      </div>
    </div>
    <div className="intro-certificate">
      <img src={assetUrl('/images/skillcert-certificate-template.webp')} alt="SkillCert certificate preview"/>
      <strong className="intro-certificate-name">Your name</strong>
      <strong className="intro-certificate-course">Your course</strong>
      <span className="five-minute-badge">Certify your skill <b>in 5 min</b></span>
    </div>
    <article key={testimonialIndex} className="intro-testimonial">
      <span className="employer-mark"><img src={testimonial.logo} alt={`${testimonial.employer} logo`}/></span>
      <div>
        <strong>{testimonial.name} <i>·</i> {testimonial.location}</strong>
        <p>{testimonial.quote}</p>
      </div>
    </article>
    <footer className="intro-sticky">
      <button className="intro-start" onClick={onStart}>Get started <ChevronRight size={26}/></button>
      <div key={activityIndex} className="intro-activity">
        {activityIndex === 0 ? <><span className="profile-stack"><i>R</i><i>A</i><i>K</i></span><span><b>797</b> certified today in under 10 min</span></> : <><span className="location-pin">●</span><span><b>300+</b> from Hyderabad scored 60%+ today. <em>Check yours.</em></span></>}
      </div>
    </footer>
  </motion.section>
}

const certificateShowcase = [
  { name: 'Your Name', course: 'Cloud Foundations' },
  { name: 'Priyanka Sharma', course: 'Digital Marketing Essentials' },
  { name: 'Varun Reddy', course: 'Data Analytics Foundations' },
  { name: 'Aanya Menon', course: 'Agile Project Management' },
]

function CertificateCarousel() {
  const [activeCertificate, setActiveCertificate] = useState(0)
  useEffect(() => {
    const interval = window.setInterval(() => setActiveCertificate(current => (current + 1) % certificateShowcase.length), 2000)
    return () => window.clearInterval(interval)
  }, [])
  const certificate = certificateShowcase[activeCertificate]
  const displayLabel = (value: string) => value.length > 30 ? `${value.slice(0, 29)}…` : value
  return <div className="certificate-stage" aria-label="Examples of SkillCert certificates">
    <i className="certificate-backdrop backdrop-one"/>
    <i className="certificate-backdrop backdrop-two"/>
    <i className="certificate-backdrop backdrop-three"/>
    <div className="certificate-glow"/>
    <div className="landing-certificate" key={certificate.name}>
        <img src={assetUrl('/images/skillcert-certificate-template.webp')} alt={`Certificate of Completion for ${certificate.name}`} />
        <strong className="certificate-name">{displayLabel(certificate.name)}</strong>
        <strong className="certificate-course">{displayLabel(certificate.course)}</strong>
      </div>
    <div className="certificate-dots" aria-hidden="true">{certificateShowcase.map((item, index) => <i className={index === activeCertificate ? 'active' : ''} key={item.name}/>)}</div>
  </div>
}

function Landing({ onStart, onExplore }: { onStart: () => void, onExplore: () => void }) { return <motion.section {...variants} className="web-page landing"><SiteNav onExplore={onExplore}/><div className="hero-copy"><div className="eyebrow">SKILLS FOR A BRIGHTER TOMORROW</div><h1>Turn knowledge into a <em>credential.</em></h1><p>Short, transparent skill assessments. A QR-verifiable credential when you meet the standard.</p><Button onClick={onExplore}>Explore assessments</Button><div className="trust-row"><span><Clock3/>5-question sprints</span><span><Database/>Server-scored</span><span><ShieldCheck/>QR-verifiable</span></div></div><CertificateCarousel/><button className="mobile-start" onClick={onStart}>Start Cloud Foundations <ChevronRight/></button></motion.section> }

const comingSoon = [
  ['Java Foundations', 'Core Java concepts and problem solving', Code2], ['AI Fundamentals', 'Essential artificial intelligence concepts', Brain], ['Agile Essentials', 'Modern delivery and team principles', Workflow], ['Data Analytics', 'Core analysis and insight skills', Database], ['Cybersecurity Basics', 'Foundational security concepts', ShieldCheck], ['Digital Marketing', 'Channels, audiences and growth', Megaphone], ['UI/UX Fundamentals', 'Human-centred design principles', Palette]
] as const

function Explorer({ catalogue, onSelect, onHome }: { catalogue: CertificationSummary[], onSelect: (slug: string) => void, onHome: () => void }) {
  const [activeCategory, setActiveCategory] = useState<string | null>(null)
  const [query, setQuery] = useState('')
  const categories = [...new Set(catalogue.map(item => item.category))]
  const available = catalogue.filter(item => item.category === activeCategory && item.title.toLowerCase().includes(query.toLowerCase()))
  return <motion.section {...variants} className="web-page explorer"><SiteNav onHome={onHome}/><header><div className="eyebrow">SKILLCERT TRACKS</div><h1>{activeCategory ? <>Choose a <em>certificate.</em></> : <>What would you like to <em>prove?</em></>}</h1><p>{activeCategory ? `Focused assessments in ${activeCategory}.` : 'Choose a skill track, then validate a focused capability.'}</p>{activeCategory && <button className="link category-back" onClick={() => { setActiveCategory(null); setQuery('') }}>← All tracks</button>}{activeCategory && <label className="search"><Search/><input value={query} onChange={event => setQuery(event.target.value)} placeholder="Search certificates"/></label>}</header><div className="card-grid">{activeCategory ? available.map(item => <button className="assessment-card featured" key={item.slug} onClick={() => onSelect(item.slug)}>{item.category === 'Programming' ? <Code2/> : <Cloud/>}<div><small>{item.questionCount} QUESTIONS · {item.durationMinutes} MIN</small><strong>{item.title}</strong><p>{item.description}</p><em>Level 1 · Certificate assessment</em></div><ChevronRight/></button>) : categories.map(category => <button className="assessment-card featured" key={category} onClick={() => setActiveCategory(category)}>{category === 'Programming' ? <Code2/> : <Cloud/>}<div><small>SKILLCERT TRACK</small><strong>{category}</strong><p>{catalogue.filter(item => item.category === category).length} assessment{catalogue.filter(item => item.category === category).length === 1 ? '' : 's'} available</p></div><ChevronRight/></button>)}</div></motion.section>
}

function Theory({ close, text }: { close: () => void, text: string | null }) { return <motion.div className="sheet-backdrop" initial={{opacity: 0}} animate={{opacity: 1}} exit={{opacity: 0}} onClick={close}><motion.aside className="sheet" initial={{y: 500}} animate={{y: 0}} transition={{type: 'spring', damping: 24}} onClick={e => e.stopPropagation()}><button className="close" onClick={close}><X/></button><span className="tag"><BookOpen/>Concept guide</span><h2>Read the concept</h2><p>{text ?? 'This question checks a practical Cloud Foundations concept.'}</p><div className="callout"><b>Why it matters</b><br/>Use this concept to make informed decisions in real cloud environments.</div><Button onClick={close}>Back to question</Button></motion.aside></motion.div> }
