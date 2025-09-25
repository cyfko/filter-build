"""
Configuration for Python adapter tests.
"""

import sys
import os

# Add core module to path
core_path = os.path.join(os.path.dirname(__file__), '..', '..', '..', 'core', 'python', 'src')
if core_path not in sys.path:
    sys.path.insert(0, core_path)

# Add adapters module to path
adapters_path = os.path.join(os.path.dirname(__file__), '..')
if adapters_path not in sys.path:
    sys.path.insert(0, adapters_path)

# Add django and sqlalchemy modules to path
django_path = os.path.join(adapters_path, 'django')
if django_path not in sys.path:
    sys.path.insert(0, django_path)

sqlalchemy_path = os.path.join(adapters_path, 'sqlalchemy')
if sqlalchemy_path not in sys.path:
    sys.path.insert(0, sqlalchemy_path)
