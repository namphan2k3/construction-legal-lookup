import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDebounce } from '@/hooks/useDebounce';
import { useAuthStore, selectIsAuthenticated } from '@/stores/authStore';
import { suggestDocuments } from '@/services/document.service';
import { getSearchHistory } from '@/services/history.service';
import styles from './SearchBar.module.css';

export function SearchBar({
  initialQuery = '',
  onSearch,
  placeholder = 'Tìm kiếm theo từ khóa, số hiệu, tiêu đề...',
  size = 'md',
  autoFocus = false,
}) {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore(selectIsAuthenticated);
  const [query, setQuery] = useState(initialQuery);
  const [suggestions, setSuggestions] = useState([]);
  const [searchHistory, setSearchHistory] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const wrapperRef = useRef(null);
  const debouncedQuery = useDebounce(query, 300);

  useEffect(() => {
    setQuery(initialQuery);
  }, [initialQuery]);

  // Load search history when authenticated
  useEffect(() => {
    if (isAuthenticated) {
      getSearchHistory()
        .then((data) => setSearchHistory(data || []))
        .catch(() => setSearchHistory([]));
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (debouncedQuery.length < 2) {
      setSuggestions([]);
      return;
    }

    let cancelled = false;
    suggestDocuments(debouncedQuery)
      .then((data) => {
        if (!cancelled) setSuggestions(data || []);
      })
      .catch(() => {
        if (!cancelled) setSuggestions([]);
      });

    return () => { cancelled = true; };
  }, [debouncedQuery]);

  useEffect(() => {
    function handleClickOutside(e) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setShowSuggestions(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    setShowSuggestions(false);
    if (onSearch) {
      onSearch(query.trim());
    } else {
      navigate(`/search?q=${encodeURIComponent(query.trim())}`);
    }
  };

  const selectSuggestion = (doc) => {
    setShowSuggestions(false);
    navigate(`/documents/${doc.id}`);
  };

  const selectHistoryItem = (historyItem) => {
    setShowSuggestions(false);
    setQuery(historyItem.query);
    if (onSearch) {
      onSearch(historyItem.query);
    } else {
      navigate(`/search?q=${encodeURIComponent(historyItem.query)}`);
    }
  };

  const goToHistoryPage = () => {
    setShowSuggestions(false);
    navigate('/history/search');
  };

  const hasSuggestions = suggestions.length > 0;
  const hasHistory = searchHistory.length > 0 && query.length === 0;
  const showDropdown = showSuggestions && (hasSuggestions || hasHistory);

  return (
    <form
      ref={wrapperRef}
      className={`${styles.searchBar} ${styles[`searchBar--${size}`]}`}
      onSubmit={handleSubmit}
      role="search"
    >
      <div className={styles.searchBar__wrapper}>
        <input
          type="search"
          className={styles.searchBar__input}
          placeholder={placeholder}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setShowSuggestions(true)}
          autoFocus={autoFocus}
          aria-label="Tìm kiếm văn bản"
          autoComplete="off"
        />
        <button type="submit" className={styles.searchBar__submit}>
          Tìm kiếm
        </button>
      </div>

      {showDropdown ? (
        <ul className={styles.searchBar__suggestions} role="listbox">
          {hasSuggestions ? (
            suggestions.map((doc) => (
              <li key={doc.id} role="option">
                <button
                  type="button"
                  className={styles.searchBar__suggestion}
                  onClick={() => selectSuggestion(doc)}
                >
                  <span className={styles.searchBar__suggestionNumber}>{doc.documentNumber}</span>
                  <span className={styles.searchBar__suggestionTitle}>{doc.title}</span>
                </button>
              </li>
            ))
          ) : hasHistory ? (
            <>
              <li className={styles.searchBar__historyHeader}>
                <span>Lịch sử tìm kiếm</span>
              </li>
              {searchHistory.slice(0, 5).map((item, index) => (
                <li key={index} role="option">
                  <button
                    type="button"
                    className={styles.searchBar__suggestion}
                    onClick={() => selectHistoryItem(item)}
                  >
                    <span className={styles.searchBar__suggestionQuery}>{item.query || '(Không có từ khóa)'}</span>
                  </button>
                </li>
              ))}
              <li className={styles.searchBar__historyFooter}>
                <button
                  type="button"
                  className={styles.searchBar__viewAllBtn}
                  onClick={goToHistoryPage}
                >
                  Xem tất cả
                </button>
              </li>
            </>
          ) : null}
        </ul>
      ) : null}
    </form>
  );
}
