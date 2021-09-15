# automation of JAVA

This is a automation framework built upon Selenium WebDriver with Object-Oriented Programming and Function Programming Paradigms.

- Makes automation test suites composed of HTML element locators and high-level functions only, and the tests would fail only when application under test fail.
- Objectified classes to abstract HTML elements (Edit, Select, Link, Checkbox and etc.) with inherited mechanism to perform:
    - pre-condition checking (such as exist, visible, enabled, option loaded);
    - experience enhanced operations like highlight, scroll;
    - default operations from a single string argument, like click, enter text, select option with multiple alternative means.
    - Post-condition checking and re-do.
    -Universal pipeline to handle any exceptions with retry mechanism to make tests hard to fail.
- Collection objects (such as List, Table, Navigator) to define multiple elements by reflection, and locate and operate them dynamically.
- Frame and search context switching automatically, which make tests easy to develop and fast to run.
- Creative utility/mechanism for test setting revoking: lambdas with all parameters would be registered after success changing of system settings and would be triggered reliably.
- Advanced logging supports: screenshots embedded into HTML report, stackframes of only concerned methods, logs of different levels could be kept based on pass/fail of the steps.

[A recorded video](https://www.youtube.com/watch?v=9FNWmu5Z9Fo) shows how this framework can launch 5 different browsers, use different search engines to perform concurrent searching activities.