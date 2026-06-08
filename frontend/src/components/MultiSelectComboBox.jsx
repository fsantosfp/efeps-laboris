import React, { useState, useEffect, useRef } from "react";
import "./MultiSelectComboBox.css";

function MultiSelectComboBox({
    options = [],
    selectedIds = [],
    onChange,
    placeholder = "Selecione...",
    labelField = "name",
    subLabelField = "",
    loading = false,
    loadingPlaceholder = "Carregando..."
}) {
    const [isOpen, setIsOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState("");
    const containerRef = useRef(null);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (containerRef.current && !containerRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    // Clear search term when dropdown closes
    useEffect(() => {
        if (!isOpen) {
            setSearchTerm("");
        }
    }, [isOpen]);

    // Toggle single item selection
    const handleToggleItem = (id) => {
        if (selectedIds.includes(id)) {
            onChange(selectedIds.filter((itemIdx) => itemIdx !== id));
        } else {
            onChange([...selectedIds, id]);
        }
    };

    // Remove single tag directly from trigger
    const handleRemoveTag = (e, id) => {
        e.stopPropagation(); // Prevent dropdown from toggling
        onChange(selectedIds.filter((itemIdx) => itemIdx !== id));
    };

    // Filter options based on search term
    const filteredOptions = options.filter((option) => {
        const label = (option[labelField] || "").toString().toLowerCase();
        const subLabel = subLabelField ? (option[subLabelField] || "").toString().toLowerCase() : "";
        const term = searchTerm.toLowerCase();
        return label.includes(term) || subLabel.includes(term);
    });

    // Select All visible options
    const handleSelectAll = (e) => {
        e.preventDefault();
        e.stopPropagation();
        const visibleIds = filteredOptions.map((opt) => opt.id);
        // Union of already selected and visible options
        const newSelected = Array.from(new Set([...selectedIds, ...visibleIds]));
        onChange(newSelected);
    };

    // Clear visible options or clear all
    const handleClearSelection = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (searchTerm) {
            // Remove only visible options
            const visibleIds = filteredOptions.map((opt) => opt.id);
            onChange(selectedIds.filter((id) => !visibleIds.includes(id)));
        } else {
            // Clear all
            onChange([]);
        }
    };

    // Helper to get selected objects
    const selectedOptions = options.filter((opt) => selectedIds.includes(opt.id));

    // Render trigger tags
    const renderTriggerContent = () => {
        if (selectedOptions.length === 0) {
            return <span className="combobox-placeholder">{placeholder}</span>;
        }

        const maxTags = 2;
        const visibleTags = selectedOptions.slice(0, maxTags);
        const remainingCount = selectedOptions.length - maxTags;

        return (
            <div className="combobox-tags-wrapper">
                {visibleTags.map((opt) => (
                    <div key={opt.id} className="combobox-tag">
                        <span className="combobox-tag-label">{opt[labelField]}</span>
                        <button
                            type="button"
                            className="combobox-tag-remove"
                            onClick={(e) => handleRemoveTag(e, opt.id)}
                        >
                            &times;
                        </button>
                    </div>
                ))}
                {remainingCount > 0 && (
                    <span className="combobox-tags-count">
                        + {remainingCount} outro{remainingCount > 1 ? "s" : ""}
                    </span>
                )}
            </div>
        );
    };

    return (
        <div className="combobox-container" ref={containerRef}>
            <div
                className={`combobox-trigger ${isOpen ? "active" : ""}`}
                onClick={() => !loading && setIsOpen(!isOpen)}
            >
                {loading ? (
                    <span className="combobox-placeholder">{loadingPlaceholder}</span>
                ) : (
                    renderTriggerContent()
                )}
                <div className="combobox-arrow">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 20 20"
                        fill="currentColor"
                        width="16"
                        height="16"
                    >
                        <path
                            fillRule="evenodd"
                            d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                            clipRule="evenodd"
                        />
                    </svg>
                </div>
            </div>

            {isOpen && (
                <div className="combobox-dropdown">
                    <div className="combobox-search-wrapper">
                        <input
                            type="text"
                            className="combobox-search-input"
                            placeholder="Pesquisar..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            autoFocus
                        />
                    </div>

                    <div className="combobox-actions">
                        <button
                            type="button"
                            className="combobox-action-btn"
                            onClick={handleSelectAll}
                            disabled={filteredOptions.length === 0}
                        >
                            Selecionar Todos
                        </button>
                        <button
                            type="button"
                            className="combobox-action-btn btn-clear"
                            onClick={handleClearSelection}
                            disabled={selectedIds.length === 0}
                        >
                            Limpar Seleção
                        </button>
                    </div>

                    <div className="combobox-list">
                        {filteredOptions.length === 0 ? (
                            <div className="combobox-no-options">Nenhum resultado encontrado.</div>
                        ) : (
                            filteredOptions.map((option) => {
                                const isChecked = selectedIds.includes(option.id);
                                return (
                                    <div
                                        key={option.id}
                                        className={`combobox-item ${isChecked ? "selected" : ""}`}
                                        onClick={() => handleToggleItem(option.id)}
                                    >
                                        <input
                                            type="checkbox"
                                            className="combobox-item-checkbox"
                                            checked={isChecked}
                                            onChange={() => {}} // Handled by container click
                                        />
                                        <div className="combobox-item-details">
                                            <span className="combobox-item-title">
                                                {option[labelField] || "Sem identificador"}
                                            </span>
                                            {subLabelField && option[subLabelField] && (
                                                <span className="combobox-item-subtitle">
                                                    {option[subLabelField]}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                );
                            })
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

export default MultiSelectComboBox;
