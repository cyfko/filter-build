"""
Setup script for the dynamic-filter-django Python package.
"""

from setuptools import setup, find_packages

with open("README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()

setup(
    name="dynamic-filter-django",
    version="1.0.0",
    author="Frank KOSSI",
    author_email="frank.kossi@kunrin.com",
    description="Django ORM adapter for dynamic filtering",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/cyfko/dynamic-filter",
    project_urls={
        "Bug Reports": "https://github.com/cyfko/dynamic-filter/issues",
        "Source": "https://github.com/cyfko/dynamic-filter",
        "Documentation": "https://github.com/cyfko/dynamic-filter#readme",
    },
    packages=find_packages(),
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
        "Topic :: Software Development :: Libraries :: Python Modules",
        "Topic :: Database",
        "Topic :: Internet :: WWW/HTTP :: Dynamic Content",
    ],
    python_requires=">=3.8",
    install_requires=[
        "dynamic-filter-core>=1.0.0",
        "Django>=3.2.0",
    ],
    extras_require={
        "dev": [
            "pytest>=7.0.0",
            "pytest-cov>=4.0.0",
            "black>=22.0.0",
            "flake8>=5.0.0",
            "mypy>=1.0.0",
        ],
    },
    keywords="filter dynamic django orm database",
    license="MIT",
    zip_safe=False,
)
