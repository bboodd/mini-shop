import React, { useState } from 'react';

function SearchBar({ onSearch, onReset }) {
  const [keyword, setKeyword] = useState('');

  const handleSearch = () => {
    if (keyword.trim()) {
      onSearch(keyword);
    }
  };

  const handleReset = () => {
    setKeyword('');
    onReset();
  };

  return (
    <div className="search-bar">
      <input
        type="text"
        placeholder="상품 검색 (Elasticsearch)"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
      />
      <button onClick={handleSearch}>검색</button>
      <button onClick={handleReset}>전체보기</button>
    </div>
  );
}

export default SearchBar;