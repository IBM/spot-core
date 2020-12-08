# spot-core
<b>Core of the Selenium Page Object Testing repository</b>

SPOT is a framework to help writing integration test scenarios using full Oriented Object design in Java.

While writing Web UI automated tests, development and test teams are facing several known problems:
- lack of reliability
- poor reusability
- high cost (or even not possible) for maintenance

At the time the framework was created, JUnit was the most simple and popular framework to write integration tests (despite of its name...). On the other side, Selenium, with the introduction of the Page Object model, appeared to be a possible solution to have the capability of automated tests entirely written with a OO design. However Selenium 2 was just made available and nothing ensure us that it will have the stability and enough functionality to match all necessary product automated tests design. So we needed to be careful about its possible unknown issues and also its versions evolution which could potentially break the automated tests based on it.

That's why we decided that the framework would need at least to:
- Enrich and possibly fix underlying frameworks (JUnit and Selenium)
- Implement a full OO Design
- Have separated layers (Core / Products / Scenarios)

Basically, the framework was developed following fundamental principles:

1) <b>Robustness</b><br>
SPOT has to be robust and should not raise any false negative (ie. due to the automated code itself). When such problems occur they should be rapidly addressed especially in Core layer.

2) <b>Ease</b><br>
SPOT hides the underlying frameworks (ie. Selenium and JUnit) by proposing an API which is applications web pages oriented. So testers do not need to know how Selenium works to write and to run tests. As they are supposed to know the product they want to test, looking at corresponding web page API methods is enough to write tests (e.g. click on a specific button, jump to another page, etc.).

3) <b>Configurable</b><br>
SPOT allows testers to run the scenario in different configurations without changing anything in the code (neither in the framework, nor in the scenario). As an example, changing some parameters value is enough to run the scenario using various browsers or targeting different product installations. Changing the scenario data is also possible using the same scenario arguments mechanism.

4) <b>Tolerant</b><br>
SPOT is resilient if a transient error occurs during a test. When that happens, there is some default mechanism to resume the scenario execution without failing. Of course in such case, tester is warned of the error by dumping all information about it, but the scenario was able to smoothly run until the end.

5) <b>Helpful</b><br>
SPOT helps testers when failure(s) occur(s) due to a product issue. All detailed debug information is dumped in a log file, and screenshots are taken in case of errors or exceptions (failures, infos, warnings) during the scenario execution.<br>
Moreover, testers can globally decide whether a scenario should stop the execution as soon as an error occurs, but it also allows deciding this for each step or test. It also allows resuming the execution from the failure if a workaround has been applied manually for example. In case the scenario has continued after the failure, it allows running the single failing test again, e.g. for debug purposes.<br><br>
SPOT also offers testers to easily run the scenario both from an IDE during the implementation phase and when scenario is finally written to simply run it from a Maven command line (i.e. either on Windows Or Linux box). That allows to speed up the implementation and tuning, and on the other side an easy usage in build pipelines.

One can find in this repository a sample developed for demo purposes which hightlights how to use the core layer to implement a product and a scenario layer. Visit the repository wiki page to find more details about this sample and some other framework functionalities.
