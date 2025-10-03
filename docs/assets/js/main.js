// Main JavaScript - FilterQL Documentation
// =============================================================================

document.addEventListener('DOMContentLoaded', function() {
    initializeThemeToggle();
    initializeMobileMenu();
    initializeNavigation();
    initializeCodeCopyButtons();
    initializeTableOfContents();
    initializeSearch();
    initializeSmoothScrolling();
    initializePerformanceOptimizations();
});

// Theme Toggle (Dark/Light Mode)
// -----------------------------------------------------------------------------
function initializeThemeToggle() {
    const themeToggle = document.getElementById('theme-toggle');
    if (!themeToggle) return;

    // Set initial theme
    const currentTheme = localStorage.getItem('darkMode') === 'true' || 
                        (!localStorage.getItem('darkMode') && window.matchMedia('(prefers-color-scheme: dark)').matches);
    
    document.documentElement.classList.toggle('dark', currentTheme);
    updateThemeToggleIcon(currentTheme);

    // Theme toggle click handler
    themeToggle.addEventListener('click', function() {
        const isDark = document.documentElement.classList.contains('dark');
        const newTheme = !isDark;
        
        document.documentElement.classList.toggle('dark', newTheme);
        localStorage.setItem('darkMode', newTheme);
        updateThemeToggleIcon(newTheme);
        
        // Smooth transition
        document.body.style.transition = 'color 0.3s ease, background-color 0.3s ease';
        setTimeout(() => {
            document.body.style.transition = '';
        }, 300);
    });

    // System theme change detection
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function(e) {
        if (!localStorage.getItem('darkMode')) {
            document.documentElement.classList.toggle('dark', e.matches);
            updateThemeToggleIcon(e.matches);
        }
    });
}

function updateThemeToggleIcon(isDark) {
    const themeToggle = document.getElementById('theme-toggle');
    if (!themeToggle) return;

    const sunIcon = themeToggle.querySelector('[data-feather="sun"]');
    const moonIcon = themeToggle.querySelector('[data-feather="moon"]');
    
    if (sunIcon && moonIcon) {
        sunIcon.style.display = isDark ? 'none' : 'block';
        moonIcon.style.display = isDark ? 'block' : 'none';
    }
}

// Mobile Menu
// -----------------------------------------------------------------------------
function initializeMobileMenu() {
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');
    const mobileMenu = document.getElementById('mobile-menu');
    const mobileMenuIcon = mobileMenuBtn?.querySelector('.mobile-menu-icon');
    const mobileCloseIcon = mobileMenuBtn?.querySelector('.mobile-close-icon');

    if (!mobileMenuBtn || !mobileMenu) return;

    let isMenuOpen = false;

    // Toggle mobile menu
    mobileMenuBtn.addEventListener('click', function() {
        isMenuOpen = !isMenuOpen;
        
        if (isMenuOpen) {
            // Show menu
            mobileMenu.classList.add('show');
            mobileMenuIcon?.classList.add('hidden');
            mobileMenuIcon?.classList.remove('block');
            mobileCloseIcon?.classList.add('block');
            mobileCloseIcon?.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
        } else {
            // Hide menu
            mobileMenu.classList.remove('show');
            mobileMenuIcon?.classList.add('block');
            mobileMenuIcon?.classList.remove('hidden');
            mobileCloseIcon?.classList.add('hidden');
            mobileCloseIcon?.classList.remove('block');
            document.body.style.overflow = '';
        }
    });

    // Close mobile menu when clicking on a link
    mobileMenu.addEventListener('click', function(e) {
        if (e.target.tagName === 'A' && !e.target.classList.contains('mobile-dropdown-toggle')) {
            isMenuOpen = false;
            mobileMenu.classList.remove('show');
            mobileMenuIcon?.classList.add('block');
            mobileMenuIcon?.classList.remove('hidden');
            mobileCloseIcon?.classList.add('hidden');
            mobileCloseIcon?.classList.remove('block');
            document.body.style.overflow = '';
        }
    });

    // Handle mobile dropdown toggles
    document.querySelectorAll('.mobile-dropdown-toggle').forEach(function(toggle) {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            const content = this.parentElement.querySelector('.mobile-dropdown-content');
            const chevron = this.querySelector('.mobile-chevron');
            
            if (content) {
                const isHidden = content.classList.contains('hidden');
                content.classList.toggle('hidden', !isHidden);
                chevron?.classList.toggle('rotate', isHidden);
            }
        });
    });

    // Close mobile menu on escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && isMenuOpen) {
            isMenuOpen = false;
            mobileMenu.classList.remove('show');
            mobileMenuIcon?.classList.add('block');
            mobileMenuIcon?.classList.remove('hidden');
            mobileCloseIcon?.classList.add('hidden');
            mobileCloseIcon?.classList.remove('block');
            document.body.style.overflow = '';
        }
    });

    // Close mobile menu on larger screens
    window.addEventListener('resize', function() {
        if (window.innerWidth >= 1024 && isMenuOpen) {
            isMenuOpen = false;
            mobileMenu.classList.remove('show');
            mobileMenuIcon?.classList.add('block');
            mobileMenuIcon?.classList.remove('hidden');
            mobileCloseIcon?.classList.add('hidden');
            mobileCloseIcon?.classList.remove('block');
            document.body.style.overflow = '';
        }
    });
}

// Navigation
// -----------------------------------------------------------------------------
function initializeNavigation() {
    // Expandable navigation sections
    document.querySelectorAll('.nav-section-title a').forEach(function(link) {
        link.addEventListener('click', function(e) {
            const subsection = this.parentElement.nextElementSibling;
            const chevron = this.querySelector('.nav-chevron');
            
            if (subsection && subsection.classList.contains('nav-subsection')) {
                e.preventDefault();
                
                const isHidden = subsection.classList.contains('hidden');
                subsection.classList.toggle('hidden', !isHidden);
                chevron?.classList.toggle('rotate-90', isHidden);
            }
        });
    });

    // Highlight active navigation item
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(function(link) {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
            
            // Expand parent section if nested
            const parentSection = link.closest('.nav-subsection');
            if (parentSection) {
                parentSection.classList.remove('hidden');
                const chevron = parentSection.previousElementSibling?.querySelector('.nav-chevron');
                chevron?.classList.add('rotate-90');
            }
        }
    });
}

// Code Copy Buttons
// -----------------------------------------------------------------------------
function initializeCodeCopyButtons() {
    document.querySelectorAll('pre code, .highlight').forEach(function(codeBlock) {
        const wrapper = document.createElement('div');
        wrapper.className = 'code-block-wrapper';
        
        const copyButton = document.createElement('button');
        copyButton.className = 'copy-button';
        copyButton.innerHTML = '<i data-feather="copy"></i>';
        copyButton.setAttribute('aria-label', 'Copy code');
        
        // Wrap the code block
        codeBlock.parentNode.insertBefore(wrapper, codeBlock);
        wrapper.appendChild(codeBlock);
        wrapper.appendChild(copyButton);
        
        copyButton.addEventListener('click', async function() {
            const code = codeBlock.textContent || codeBlock.innerText;
            
            try {
                await navigator.clipboard.writeText(code);
                copyButton.innerHTML = '<i data-feather="check"></i>';
                copyButton.style.backgroundColor = 'rgba(34, 197, 94, 0.8)';
                
                setTimeout(function() {
                    copyButton.innerHTML = '<i data-feather="copy"></i>';
                    copyButton.style.backgroundColor = '';
                }, 2000);
            } catch (err) {
                // Fallback for older browsers
                const textArea = document.createElement('textarea');
                textArea.value = code;
                document.body.appendChild(textArea);
                textArea.select();
                document.execCommand('copy');
                document.body.removeChild(textArea);
                
                copyButton.innerHTML = '<i data-feather="check"></i>';
                setTimeout(function() {
                    copyButton.innerHTML = '<i data-feather="copy"></i>';
                }, 2000);
            }
        });
    });
    
    // Re-initialize Feather icons for new copy buttons
    if (typeof feather !== 'undefined') {
        feather.replace();
    }
}

// Table of Contents
// -----------------------------------------------------------------------------
function initializeTableOfContents() {
    const tocContainer = document.getElementById('toc');
    if (!tocContainer) return;

    const headings = document.querySelectorAll('h2, h3, h4');
    if (headings.length === 0) {
        tocContainer.parentElement.style.display = 'none';
        return;
    }

    const tocList = document.createElement('ul');
    let currentLevel = 2;
    let currentList = tocList;
    const listStack = [tocList];

    headings.forEach(function(heading, index) {
        const level = parseInt(heading.tagName.charAt(1));
        const id = heading.id || `heading-${index}`;
        
        if (!heading.id) {
            heading.id = id;
        }

        const listItem = document.createElement('li');
        const link = document.createElement('a');
        link.href = `#${id}`;
        link.textContent = heading.textContent;
        link.className = 'toc-link';
        
        // Handle nesting
        if (level > currentLevel) {
            const nestedList = document.createElement('ul');
            const lastItem = currentList.lastElementChild;
            if (lastItem) {
                lastItem.appendChild(nestedList);
            }
            currentList = nestedList;
            listStack.push(nestedList);
        } else if (level < currentLevel) {
            for (let i = currentLevel; i > level; i--) {
                listStack.pop();
                currentList = listStack[listStack.length - 1];
            }
        }
        
        currentLevel = level;
        listItem.appendChild(link);
        currentList.appendChild(listItem);

        // Smooth scroll on click
        link.addEventListener('click', function(e) {
            e.preventDefault();
            document.getElementById(id)?.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
            
            // Update active state
            document.querySelectorAll('.toc-link').forEach(l => l.classList.remove('active'));
            link.classList.add('active');
        });
    });

    tocContainer.appendChild(tocList);

    // Highlight current section on scroll
    let isScrolling = false;
    window.addEventListener('scroll', function() {
        if (!isScrolling) {
            window.requestAnimationFrame(function() {
                updateActiveHeading();
                isScrolling = false;
            });
            isScrolling = true;
        }
    });
}

function updateActiveHeading() {
    const headings = document.querySelectorAll('h2, h3, h4');
    const tocLinks = document.querySelectorAll('.toc-link');
    
    let activeHeading = null;
    const scrollPosition = window.scrollY + 100;

    headings.forEach(function(heading) {
        if (heading.offsetTop <= scrollPosition) {
            activeHeading = heading;
        }
    });

    tocLinks.forEach(function(link) {
        link.classList.remove('active');
        if (activeHeading && link.getAttribute('href') === `#${activeHeading.id}`) {
            link.classList.add('active');
        }
    });
}

// Search Functionality
// -----------------------------------------------------------------------------
function initializeSearch() {
    const searchBtn = document.getElementById('search-btn');
    const searchModal = document.getElementById('search-modal');
    const searchInput = document.getElementById('search-input');
    const searchClose = document.getElementById('search-close');
    const searchResults = document.getElementById('search-results');

    if (!searchBtn || !searchModal) return;

    // Open search modal
    searchBtn.addEventListener('click', function() {
        searchModal.classList.remove('hidden');
        searchInput?.focus();
        document.body.style.overflow = 'hidden';
    });

    // Close search modal
    function closeSearch() {
        searchModal.classList.add('hidden');
        document.body.style.overflow = '';
        if (searchInput) searchInput.value = '';
        if (searchResults) searchResults.innerHTML = getDefaultSearchContent();
    }

    searchClose?.addEventListener('click', closeSearch);
    
    // Close on escape or click outside
    searchModal.addEventListener('click', function(e) {
        if (e.target === searchModal) closeSearch();
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && !searchModal.classList.contains('hidden')) {
            closeSearch();
        }
        // Open search with Ctrl/Cmd + K
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            searchBtn.click();
        }
    });

    // Search functionality (basic implementation)
    let searchTimeout;
    searchInput?.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            performSearch(this.value);
        }, 300);
    });
}

function getDefaultSearchContent() {
    return `
        <div class="p-8 text-center text-gray-500 dark:text-gray-400">
            <i data-feather="search" class="w-8 h-8 mx-auto mb-4 opacity-50"></i>
            <p>Start typing to search the documentation...</p>
        </div>
    `;
}

function performSearch(query) {
    const searchResults = document.getElementById('search-results');
    if (!searchResults) return;

    if (!query.trim()) {
        searchResults.innerHTML = getDefaultSearchContent();
        if (typeof feather !== 'undefined') feather.replace();
        return;
    }

    // Basic search implementation (you can replace with more sophisticated search)
    const results = searchDocumentation(query);
    
    if (results.length === 0) {
        searchResults.innerHTML = `
            <div class="p-8 text-center text-gray-500 dark:text-gray-400">
                <i data-feather="search" class="w-8 h-8 mx-auto mb-4 opacity-50"></i>
                <p>No results found for "${query}"</p>
            </div>
        `;
    } else {
        searchResults.innerHTML = results.map(result => `
            <a href="${result.url}" class="block p-4 border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
                <h3 class="font-medium text-gray-900 dark:text-white">${result.title}</h3>
                <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">${result.excerpt}</p>
            </a>
        `).join('');
    }
    
    if (typeof feather !== 'undefined') feather.replace();
}

function searchDocumentation(query) {
    // This is a basic implementation. In production, you'd want to use
    // a proper search solution like Algolia or Jekyll's built-in search
    const pages = [
        { title: 'Getting Started', url: '/getting-started', excerpt: 'Your 10-minute journey to FilterQL mastery' },
        { title: 'Core Module', url: '/core-module', excerpt: 'Explore the architecture and internals of FilterQL' },
        { title: 'Spring Integration', url: '/spring-adapter', excerpt: 'Seamlessly integrate FilterQL with Spring Data JPA' },
        { title: 'Examples', url: '/examples', excerpt: 'Real-world examples and patterns' },
        { title: 'FAQ', url: '/faq', excerpt: 'Common questions and answers' },
        { title: 'Troubleshooting', url: '/troubleshooting', excerpt: 'Solutions for common issues' }
    ];

    const lowerQuery = query.toLowerCase();
    return pages.filter(page => 
        page.title.toLowerCase().includes(lowerQuery) ||
        page.excerpt.toLowerCase().includes(lowerQuery)
    );
}

// Smooth Scrolling
// -----------------------------------------------------------------------------
function initializeSmoothScrolling() {
    // Handle anchor links
    document.querySelectorAll('a[href^="#"]').forEach(function(link) {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href === '#') return;
            
            const target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// Performance Optimizations
// -----------------------------------------------------------------------------
function initializePerformanceOptimizations() {
    // Lazy load images
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                        img.removeAttribute('data-src');
                        imageObserver.unobserve(img);
                    }
                }
            });
        });

        document.querySelectorAll('img[data-src]').forEach(function(img) {
            imageObserver.observe(img);
        });
    }

    // Preload critical resources
    const criticalLinks = document.querySelectorAll('a[href^="/getting-started"], a[href^="/core-module"]');
    criticalLinks.forEach(function(link) {
        link.addEventListener('mouseenter', function() {
            const linkEl = document.createElement('link');
            linkEl.rel = 'prefetch';
            linkEl.href = this.href;
            document.head.appendChild(linkEl);
        }, { once: true });
    });
}

// Utility Functions
// -----------------------------------------------------------------------------
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}