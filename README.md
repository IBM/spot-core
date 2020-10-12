# spot-core
Core of the Selenium Page Object Test repository

SPOT is a framework to help writing integration test scenarios using full Oriented Object design in Java.

While writing Web UI automated tests, development and test teams are facing several known problems:
- lack of reliability
- poor reusability
- high cost (or even not possible) for maintenance

At the time the framework was created, JUnit was the most simple and popular framework to write integration tests (despite of its name...) and Selenium, with the introduction of the Page Object model, appeared to be a possible solution to have a the capability of automated tests entirely written with a OO design.

However, in 2011, Selenium 2 was just made available and nothing ensure us that it will have the stability and enough functionality to match all necessary product automated tests design. Hence, SPOT framework was created in order to: 
- Enrich/Fix underlying frameworks (JUnit and Selenium)
- Implement a full OO Design
- Have separated layers (Core / Products / Scenarios)

The framework was developped following fundamental principles:
1) Robustness
2) Reusability
3) Tolerance
4) Configurability
