import { useEffect, useState, useRef } from 'react';
import { useParams, useSearchParams, Link } from 'react-router-dom';
import { Badge } from '@/components/common/Badge/Badge';
import { Button } from '@/components/common/Button/Button';
import { Card } from '@/components/common/Card/Card';
import { LoadingOverlay } from '@/components/common/Spinner/Spinner';
import { useAuthStore, selectIsAuthenticated } from '@/stores/authStore';
import { getDocumentDetail } from '@/services/document.service';
import { addBookmark, removeBookmark } from '@/services/bookmark.service';
import { summarizeDocument, askDocument, explainText, getAiQuota } from '@/services/ai.service';
import { getErrorMessage } from '@/services/api';
import {
  formatDate,
  getDocumentTypeLabel,
  getStatusLabel,
  getStatusVariant,
} from '@/utils/formatters';
import { formatAiSummary } from '@/utils/documentHelpers';
import toast from 'react-hot-toast';
import styles from './DocumentDetailPage.module.css';

export default function DocumentDetailPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const highlight = searchParams.get('q') || searchParams.get('highlight') || '';
  const isAuthenticated = useAuthStore(selectIsAuthenticated);

  const [docData, setDocData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [bookmarked, setBookmarked] = useState(false);
  const [aiSummary, setAiSummary] = useState(null);
  const [aiAnswer, setAiAnswer] = useState(null);
  const [aiExplanation, setAiExplanation] = useState(null);
  const [selectedText, setSelectedText] = useState('');
  const [selectionPosition, setSelectionPosition] = useState(null);
  const [showAiDialog, setShowAiDialog] = useState(false);
  const [aiDialogQuestion, setAiDialogQuestion] = useState('');
  const [aiDialogHistory, setAiDialogHistory] = useState([]);
  const [aiDialogLoading, setAiDialogLoading] = useState(false);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [askLoading, setAskLoading] = useState(false);
  const [showSummaryResult, setShowSummaryResult] = useState(true);
  const [showAnswerResult, setShowAnswerResult] = useState(true);
  const [aiQuestion, setAiQuestion] = useState('');
  const [quota, setQuota] = useState(null);
  const selectionTimeoutRef = useRef(null);
  const lastSelectedTextRef = useRef('');
  const isSelectingRef = useRef(false);

  useEffect(() => {
    setLoading(true);
    getDocumentDetail(id, highlight || undefined)
      .then((detail) => {
        setDocData(detail);
        setBookmarked(detail.bookmarked || false);
      })
      .catch((err) => {
        toast.error(getErrorMessage(err, 'Không tải được văn bản'));
      })
      .finally(() => setLoading(false));
  }, [id, highlight]);

  useEffect(() => {
    if (isAuthenticated) {
      getAiQuota().then(setQuota).catch(() => {});
    }
  }, [isAuthenticated]);

  // Close popup when clicking outside
  useEffect(() => {
    try {
      const handleClickOutside = (e) => {
        if (selectedText && !e.target.closest(`.${styles.explainPopup}`)) {
          setSelectedText('');
          setSelectionPosition(null);
        }
      };

      if (window.document) {
        window.document.addEventListener('click', handleClickOutside);
        return () => {
          try {
            if (window.document) {
              window.document.removeEventListener('click', handleClickOutside);
            }
          } catch (e) {
            // Ignore cleanup errors
          }
        };
      }
    } catch (e) {
      // Ignore setup errors
    }
  }, [selectedText]);

  // Track text selection - temporarily disabled to debug selection issue
  /*
  useEffect(() => {
    let stableTimeout = null;

    const handleMouseUp = (e) => {
      // Clear any existing timeout
      if (stableTimeout) {
        clearTimeout(stableTimeout);
      }
      
      // Wait for selection to stabilize
      stableTimeout = setTimeout(() => {
        if (typeof window === 'undefined' || !window.document) return;
        
        const selection = window.getSelection();
        const text = selection.toString().trim();
        
        if (text && text.length > 3 && isAuthenticated) {
          const range = selection.getRangeAt(0);
          const rect = range.getBoundingClientRect();
          
          setSelectedText(text);
          setSelectionPosition({
            top: rect.bottom + window.scrollY + 8,
            left: rect.left + window.scrollX + (rect.width / 2)
          });
        } else {
          setSelectedText('');
          setSelectionPosition(null);
        }
      }, 200);
    };

    // Only attach to document-content element, not entire document
    const contentElement = window.document.querySelector('.document-content');
    if (contentElement) {
      contentElement.addEventListener('mouseup', handleMouseUp);
      return () => {
        if (stableTimeout) {
          clearTimeout(stableTimeout);
        }
        if (selectionTimeoutRef.current) {
          clearTimeout(selectionTimeoutRef.current);
        }
        if (contentElement) {
          contentElement.removeEventListener('mouseup', handleMouseUp);
        }
      };
    }
  }, [isAuthenticated]);
  */

  const handleBookmark = async () => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập để lưu văn bản');
      return;
    }
    try {
      if (bookmarked) {
        await removeBookmark(id);
        setBookmarked(false);
        toast.success('Đã xóa khỏi yêu thích');
      } else {
        await addBookmark(id);
        setBookmarked(true);
        toast.success('Đã thêm vào yêu thích');
      }
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  };

  const handleSummarize = async () => {
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập để dùng AI');
      return;
    }
    setSummaryLoading(true);
    try {
      const result = await summarizeDocument(id);
      setAiSummary(result);
      setShowSummaryResult(true);
      toast.success('Đã tóm tắt văn bản');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Không thể tóm tắt'));
    } finally {
      setSummaryLoading(false);
    }
  };

  const handleAsk = async (e) => {
    e.preventDefault();
    if (!aiQuestion.trim()) return;
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập để dùng AI');
      return;
    }
    setAskLoading(true);
    try {
      const result = await askDocument(id, aiQuestion.trim());
      setAiAnswer(result);
      setShowAnswerResult(true);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Không thể trả lời'));
    } finally {
      setAskLoading(false);
    }
  };

  const handleTextSelection = () => {
    const selection = window.getSelection();
    const text = selection.toString().trim();
    if (text && text.length > 10) {
      setSelectedText(text);
      
      // Get selection position
      const range = selection.getRangeAt(0);
      const rect = range.getBoundingClientRect();
      setSelectionPosition({
        top: rect.bottom + window.scrollY + 8,
        left: rect.left + window.scrollX + (rect.width / 2)
      });
    } else {
      setSelectedText('');
      setSelectionPosition(null);
    }
  };

  const handleOpenAiDialog = () => {
    setShowAiDialog(true);
    setAiDialogQuestion('');
    setAiDialogHistory([]);
    setSelectedText('');
    setSelectionPosition(null);
  };

  const handleCloseAiDialog = () => {
    setShowAiDialog(false);
    setAiDialogQuestion('');
    setAiDialogHistory([]);
  };

  const handleQuickQuestion = (question) => {
    setAiDialogQuestion(question);
  };

  const handleSendAiQuestion = async () => {
    if (!aiDialogQuestion.trim() || !selectedText.trim()) return;
    if (!isAuthenticated) {
      toast.error('Vui lòng đăng nhập để dùng AI');
      return;
    }
    
    setAiDialogLoading(true);
    try {
      const result = await explainText(id, { selectedText, question: aiDialogQuestion });
      setAiDialogHistory([...aiDialogHistory, { question: aiDialogQuestion, answer: result.explanation }]);
      setAiDialogQuestion('');
    } catch (err) {
      toast.error(getErrorMessage(err, 'Không thể trả lời'));
    } finally {
      setAiDialogLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page">
        <div className="container">
          <LoadingOverlay message="Đang tải văn bản..." />
        </div>
      </div>
    );
  }

  if (!docData) {
    return (
      <div className="page">
        <div className="container">
          <p>Không tìm thấy văn bản.</p>
          <Link to="/search">Quay lại tra cứu</Link>
        </div>
      </div>
    );
  }

  const contentHtml = docData.contentHtml;
  const contentText = docData.contentText;

  return (
    <div className="page">
      <div className="container">
        <nav className={styles.breadcrumb}>
          <Link to="/">Trang chủ</Link>
          <span aria-hidden="true"> / </span>
          <Link to="/search">Tra cứu</Link>
          <span aria-hidden="true"> / </span>
          <span>{docData.documentNumber}</span>
        </nav>

        <header className={styles.header}>
          <div className={styles.header__badges}>
            <Badge variant="primary">{getDocumentTypeLabel(docData.documentType)}</Badge>
            <Badge variant={getStatusVariant(docData.status)}>
              {getStatusLabel(docData.status)}
            </Badge>
          </div>
          <h1 className={styles.header__title}>{docData.title}</h1>
          <p className={styles.header__number}>{docData.documentNumber}</p>

          <dl className={styles.meta}>
            {docData.issuingBody ? (
              <>
                <dt>Cơ quan ban hành</dt>
                <dd>{docData.issuingBody}</dd>
              </>
            ) : null}
            {docData.signer ? (
              <>
                <dt>Người ký</dt>
                <dd>{docData.signer}</dd>
              </>
            ) : null}
            {docData.issuedDate ? (
              <>
                <dt>Ngày ban hành</dt>
                <dd>{formatDate(docData.issuedDate)}</dd>
              </>
            ) : null}
            {docData.effectiveDate ? (
              <>
                <dt>Ngày hiệu lực</dt>
                <dd>{formatDate(docData.effectiveDate)}</dd>
              </>
            ) : null}
            <dt>Lượt xem</dt>
            <dd>{docData.viewCount}</dd>
          </dl>

          {docData.abstract ? (
            <p className={styles.abstract}>{docData.abstract}</p>
          ) : null}

          <div className={styles.actions}>
            <Button
              variant={bookmarked ? 'accent' : 'secondary'}
              onClick={handleBookmark}
            >
              {bookmarked ? 'Đã lưu' : 'Lưu văn bản'}
            </Button>
          </div>
        </header>

        <div className={styles.body}>
          <div className={styles.content}>
            {contentHtml ? (
              <article
                className="document-content"
                dangerouslySetInnerHTML={{ __html: contentHtml }}
              />
            ) : contentText ? (
              <article className="document-content">
                {contentText.split('\n').map((para, i) => (
                  para.trim() ? <p key={i}>{para}</p> : null
                ))}
              </article>
            ) : (
              <p className={styles.noContent}>Nội dung văn bản chưa được cập nhật.</p>
            )}
          </div>
        </div>

        {selectedText && isAuthenticated && selectionPosition ? (
          <div 
            className={styles.explainPopup}
            style={{
              top: `${selectionPosition.top}px`,
              left: `${selectionPosition.left}px`,
              transform: 'translateX(-50%)'
            }}
          >
            <Button variant="primary" size="sm" onClick={handleOpenAiDialog}>
              🤖 Hỏi AI
            </Button>
            <Button variant="secondary" size="sm" onClick={() => {
              setSelectedText('');
              setSelectionPosition(null);
            }}>
              Đóng
            </Button>
          </div>
        ) : null}

        {showAiDialog && isAuthenticated ? (
          <div className={styles.aiDialogOverlay} onClick={handleCloseAiDialog}>
            <div className={styles.aiDialog} onClick={(e) => e.stopPropagation()}>
              <div className={styles.aiDialogHeader}>
                <h2>Hỏi AI về đoạn văn này</h2>
                <Button variant="secondary" size="sm" onClick={handleCloseAiDialog}>
                  Đóng
                </Button>
              </div>
              
              <div className={styles.aiDialogContent}>
                <div className={styles.selectedTextSection}>
                  <h3>Đoạn đã chọn:</h3>
                  <div className={styles.selectedText}>
                    {selectedText}
                  </div>
                </div>

                <div className={styles.quickQuestions}>
                  <h3>Gợi ý:</h3>
                  <div className={styles.quickQuestionButtons}>
                    <Button 
                      variant="secondary" 
                      size="sm" 
                      onClick={() => handleQuickQuestion('Giải thích đoạn này')}
                    >
                      Giải thích đoạn này
                    </Button>
                    <Button 
                      variant="secondary" 
                      size="sm" 
                      onClick={() => handleQuickQuestion('Tóm tắt đoạn này')}
                    >
                      Tóm tắt đoạn này
                    </Button>
                    <Button 
                      variant="secondary" 
                      size="sm" 
                      onClick={() => handleQuickQuestion('Đoạn này quy định điều gì?')}
                    >
                      Đoạn này quy định điều gì?
                    </Button>
                    <Button 
                      variant="secondary" 
                      size="sm" 
                      onClick={() => handleQuickQuestion('Đối tượng áp dụng là ai?')}
                    >
                      Đối tượng áp dụng là ai?
                    </Button>
                    <Button 
                      variant="secondary" 
                      size="sm" 
                      onClick={() => handleQuickQuestion('Điều kiện được quy định là gì?')}
                    >
                      Điều kiện được quy định là gì?
                    </Button>
                  </div>
                </div>

                <div className={styles.questionSection}>
                  <h3>Câu hỏi:</h3>
                  <textarea
                    className={styles.questionInput}
                    placeholder="Nhập câu hỏi của bạn..."
                    value={aiDialogQuestion}
                    onChange={(e) => setAiDialogQuestion(e.target.value)}
                    maxLength={500}
                  />
                  <div className={styles.charCount}>
                    {aiDialogQuestion.length}/500
                  </div>
                  <Button 
                    variant="primary" 
                    onClick={handleSendAiQuestion}
                    loading={aiDialogLoading}
                    disabled={!aiDialogQuestion.trim()}
                  >
                    Gửi
                  </Button>
                </div>

                {aiDialogHistory.length > 0 && (
                  <div className={styles.chatHistory}>
                    <h3>Lịch sử hội thoại:</h3>
                    {aiDialogHistory.map((item, index) => (
                      <div key={index} className={styles.chatMessage}>
                        <div className={styles.chatQuestion}>
                          <strong>Câu hỏi:</strong> {item.question}
                        </div>
                        <div className={styles.chatAnswer}>
                          <strong>Trả lời:</strong> {item.answer}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        ) : null}

        {isAuthenticated ? (
          <section className={styles.ai}>
            <h2 className={styles.ai__title}>Hỗ trợ AI</h2>
            {quota ? (
              <p className={styles.ai__quota}>
                Còn {quota.remaining ?? quota.remainingRequests ?? '—'} / {quota.limit ?? quota.dailyLimit ?? '—'} lượt hôm nay
              </p>
            ) : null}
            <p className={styles.ai__disclaimer}>
              Nội dung AI chỉ mang tính tham khảo, không thay thế tư vấn pháp lý chính thức.
            </p>

            <div className={styles.ai__actions}>
              <Button variant="secondary" loading={summaryLoading} onClick={handleSummarize}>
                Tóm tắt văn bản
              </Button>
            </div>

            {aiSummary ? (
              <Card className={`${styles.ai__result} ${!showSummaryResult ? styles['ai__result--collapsed'] : ''}`} padding="md">
                <div className={styles.ai__resultHeader}>
                  <h3>Tóm tắt</h3>
                  <button
                    type="button"
                    className={styles.ai__resultToggle}
                    onClick={() => setShowSummaryResult((v) => !v)}
                    aria-expanded={showSummaryResult}
                  >
                    {showSummaryResult ? 'Ẩn kết quả' : 'Hiện kết quả'}
                  </button>
                </div>
                {showSummaryResult ? (
                  <p>{formatAiSummary(aiSummary.summary || aiSummary)}</p>
                ) : null}
              </Card>
            ) : null}

            {aiSummary ? (
              <hr className={styles.ai__divider} />
            ) : null}

            <form className={styles.ai__form} onSubmit={handleAsk}>
              <input
                type="text"
                className={styles.ai__input}
                placeholder="Đặt câu hỏi về văn bản này..."
                value={aiQuestion}
                onChange={(e) => setAiQuestion(e.target.value)}
              />
              <Button type="submit" variant="primary" loading={askLoading}>
                Hỏi AI
              </Button>
            </form>

            {aiAnswer ? (
              <Card className={`${styles.ai__result} ${!showAnswerResult ? styles['ai__result--collapsed'] : ''}`} padding="md">
                <div className={styles.ai__resultHeader}>
                  <h3>Trả lời</h3>
                  <button
                    type="button"
                    className={styles.ai__resultToggle}
                    onClick={() => setShowAnswerResult((v) => !v)}
                    aria-expanded={showAnswerResult}
                  >
                    {showAnswerResult ? 'Ẩn kết quả' : 'Hiện kết quả'}
                  </button>
                </div>
                {showAnswerResult ? (
                  <>
                    <p>{aiAnswer.answer}</p>
                    {aiAnswer.sources?.length ? (
                      <div className={styles.ai__sources}>
                        <p className={styles.ai__sourcesTitle}>Nguồn tham chiếu</p>
                        {aiAnswer.sources.map((src, i) => (
                          <p key={i} className={styles.ai__source}>
                            {src.sectionLabel}: {src.excerpt}
                          </p>
                        ))}
                      </div>
                    ) : null}
                  </>
                ) : null}
              </Card>
            ) : null}

            {aiExplanation ? (
              <Card className={styles.ai__result} padding="md">
                <h3>Giải thích</h3>
                <p>{aiExplanation.explanation}</p>
                {aiExplanation.sources?.length ? (
                  <div className={styles.ai__sources}>
                    <p className={styles.ai__sourcesTitle}>Nguồn tham chiếu</p>
                    {aiExplanation.sources.map((src, i) => (
                      <p key={i} className={styles.ai__source}>
                        {src.sectionLabel}: {src.excerpt}
                      </p>
                    ))}
                  </div>
                ) : null}
              </Card>
            ) : null}
          </section>
        ) : null}
      </div>
    </div>
  );
}
