import { useState, useRef, useEffect } from 'react';
import { MessageCircle, X, Send, Loader2, Sparkles } from 'lucide-react';
import styles from './FloatingChatbot.module.css';
import { Input } from '@/components/common/Input/Input';
import { formatDate } from '@/utils/formatters';
import { api } from '@/services/api';

const SUGGESTED_QUESTIONS = [
  'Làm thế nào để xin giấy phép xây dựng?',
  'Mật độ xây dựng tối đa là bao nhiêu?',
  'Quy hoạch 1/500 là gì?',
  'Điều kiện khởi công xây dựng?',
  'Quy định về chỉ giới đường đỏ?'
];

const FloatingChatbot = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      role: 'assistant',
      content: `Chào bạn! Tôi là trợ lý pháp lý chuyên về lĩnh vực xây dựng tại Việt Nam.

Tôi có thể giúp bạn với các vấn đề như:
• Giấy phép xây dựng
• Quy hoạch đô thị
• Hợp đồng xây dựng
• An toàn lao động
• Vật tư xây dựng
• Giấy phép PCCC

Bạn có câu hỏi nào không?`,
      timestamp: new Date()
    }
  ]);
  const [inputText, setInputText] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [quota, setQuota] = useState(0);
  const [quotaLimit, setQuotaLimit] = useState(20);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Fetch quota when chat opens
  useEffect(() => {
    if (isOpen) {
      fetchQuota();
    }
  }, [isOpen]);

  const fetchQuota = async () => {
    try {
      const response = await api.get('/ai/quota');
      if (response.data.success) {
        setQuota(response.data.data.usedToday);
        setQuotaLimit(response.data.data.dailyLimit);
      }
    } catch (error) {
      console.error('Lỗi lấy quota:', error);
    }
  };

  const sendMessage = async (text = inputText) => {
    const trimmedText = text.trim();
    if (!trimmedText || isLoading) return;

    const userMessage = {
      id: Date.now(),
      role: 'user',
      content: trimmedText,
      timestamp: new Date()
    };

    setMessages(prev => [...prev, userMessage]);
    setInputText('');
    setIsLoading(true);

    try {
      const response = await api.post('/ai/general-chat', {
        question: trimmedText
      });

      if (response.data.success) {
        const assistantMessage = {
          id: Date.now() + 1,
          role: 'assistant',
          content: response.data.data.answer,
          sources: response.data.data.sources,
          timestamp: new Date()
        };
        setMessages(prev => [...prev, assistantMessage]);
        fetchQuota();
      }
    } catch (error) {
      console.error('Lỗi gửi tin nhắn:', error);
      const errorMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: 'Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau!',
        timestamp: new Date()
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <>
      {/* Chatbot Button */}
      <button
        className={styles.chatButton}
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Mở chatbot"
      >
        {isOpen ? <X size={24} /> : <MessageCircle size={24} />}
        <span className={styles.chatButton__badge}>Trợ lý AI</span>
      </button>

      {/* Chatbot Window */}
      {isOpen && (
        <div className={styles.chatWindow}>
          {/* Header */}
          <div className={styles.chatWindow__header}>
            <div className={styles.chatWindow__headerLeft}>
              <Sparkles size={20} className={styles.chatWindow__headerIcon} />
              <div>
                <h3 className={styles.chatWindow__title}>Trợ lý Pháp lý Xây dựng</h3>
                <p className={styles.chatWindow__subtitle}>
                  Hỏi đáp về luật xây dựng Việt Nam
                </p>
              </div>
            </div>
            <div className={`${styles.quotaBadge} ${quota >= quotaLimit - 3 ? styles.quotaBadgeWarning : ''}`}>
              {quota}/{quotaLimit} lượt hôm nay
            </div>
          </div>

          {/* Messages */}
          <div className={styles.chatWindow__messages}>
            {messages.map((message) => (
              <div
                key={message.id}
                className={`${styles.chatMessage} ${
                  message.role === 'user'
                    ? styles['chatMessage--user']
                    : styles['chatMessage--assistant']
                }`}
              >
                <div className={styles.chatMessage__avatar}>
                  {message.role === 'user' ? 'Bạn' : 'AI'}
                </div>
                <div className={styles.chatMessage__content}>
                  <div className={styles.chatMessage__text}>
                    {message.content.split('\n').map((line, index) => (
                      <p key={index}>{line}</p>
                    ))}
                  </div>
                  {message.sources && message.sources.length > 0 && (
                    <div className={styles.chatMessage__sources}>
                      <strong>Nguồn tham khảo:</strong>
                      <ul>
                        {message.sources.map((source, index) => (
                          <li key={index}>
                            {source.title} ({source.documentNumber})
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                  <span className={styles.chatMessage__time}>
                    {formatDate(message.timestamp)}
                  </span>
                </div>
              </div>
            ))}
            {isLoading && (
              <div className={`${styles.chatMessage} ${styles['chatMessage--assistant']}`}>
                <div className={styles.chatMessage__avatar}>AI</div>
                <div className={styles.chatMessage__content}>
                  <Loader2 className={styles.chatMessage__loading} />
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Suggested Questions */}
          {messages.length <= 2 && (
            <div className={styles.chatWindow__suggestions}>
              {SUGGESTED_QUESTIONS.map((question, index) => (
                <button
                  key={index}
                  className={styles.chatWindow__suggestion}
                  onClick={() => sendMessage(question)}
                  disabled={isLoading || quota >= quotaLimit}
                >
                  {question}
                </button>
              ))}
            </div>
          )}

          {/* Input */}
          <div className={styles.chatWindow__input}>
            <Input
              placeholder="Nhập câu hỏi của bạn..."
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyPress={handleKeyPress}
              disabled={isLoading}
              fullWidth
            />
            <button
              className={styles.sendButton}
              onClick={() => sendMessage()}
              disabled={isLoading || !inputText.trim() || quota >= quotaLimit}
            >
              {isLoading ? <Loader2 size={20} className={styles.sendButton__loading} /> : <Send size={20} />}
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default FloatingChatbot;
