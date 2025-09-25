"""
Pytest configuration and fixtures for dynamic filter core tests.
"""

import pytest
import sys
import os

# Add the src directory to the Python path so we can import the module
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'src'))
