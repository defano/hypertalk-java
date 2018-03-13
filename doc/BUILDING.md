# Building the application

WyldCard is built with Gradle and should easily import into any IDE with Gradle integration like Eclipse or IntelliJ.

**Note that the project makes use of generated source code.** The application will not compile (or import correctly into Eclipse) until these sources are generated via `gradle generateGrammarSource`.

The following Gradle tasks are defined by the project's build file:

Task                     | Description
-------------------------|----------------------------
`run`                    | Build, test and run the application
`generateGrammarSource`  | Re-generate the HyperTalk parser with Antlr4 (executes automatically as part of the `gradle build` task)
`generateBundle`         | Generates installation packages for the current OS (i.e., a `.dmg` disk image and `.pkg` installer on macOS), plus an executable JAR file and JNLP (network launch) files.
`clean`                  | Removes generated Antlr source and install bundles created by the `generateGrammarSource` and `generateBundle` tasks.

The project uses Antlr4 as the parser generator and the IntelliJ GUI Designer for much of the Swing UI development (see the section below for information about modifying UI components). It was originally implemented using JCup/JFlex and was converted to Antlr in July, 2016. The JCup implementation can be found in the (abandoned) `jcup` branch.

## Running the program

Execute the `gradle run` task to build and start the program or simply execute the `WyldCard` class. Once the program is running, you'll be presented with the application's main window. From here you may:

*	Begin adding your own user interface elements by choosing "New Button" or "New Field" from the "Objects" menu.
* Use the paint tools (choose "Tools Palette" from the "Tools" menu) to draw on the card.
*	Open a previously saved stack document ("File" -> "Open Stack...").
*	Enter a statement or expression in the message box ("Go" -> "Message"). Press enter to execute or evaluate your message.

To start scripting:

1.	Create a new button or field, or, choose the button tool or field tool from the tools palette, then select an existing one.
2.	Double-click on the selected part to show the part's property editor, then click the "Script..." button.

## Modifying the HyperTalk language

WyldCard uses Antrl 4 as its parser generator and utilizes the Antlr *tree visitor* pattern to convert Antlr's parse tree into a HyperTalk abstract syntax tree (a simple example of this [can be found on Stack Overflow](http://stackoverflow.com/questions/23092081/antlr4-visitor-pattern-on-simple-arithmetic-example)).

The HyperTalk grammar is defined in `HyperTalk.g4` and the tree visitor (responsible for producing nodes in the abstract syntax tree) is `HyperTalkTreeVisitor.java`. When adding new grammar rules, note that the value to the right of the `#` symbol defines the name of the visitor method associated with that rule.

For example, consider this grammar rule:

```
'show' part       # showCmdStmnt
```

The AST node associated with the `'show' part` command is created by the visitor method `visitShowCmdStmnt` in the `HyperTalkTreeVisitor.java` class.

Any changes made to the HyperTalk grammar file (`HyperTalk.g4`) require the parser to be regenerated. The can be accomplished by:

* Re-executing the `gradle generateGrammarSource` target, or
* If using InteliJ with the Antlr plugin and you wish to automatically regenerate the parser each time you modify the grammar then right-click inside the `.g4` file, choose "Configure ANTLR..."; set the "Output directory where all output is generated" to `hypertalk-java/generated-src/` and check the "generate parse tree visitor" option.

Once the parser has been regenerated, you'll be ready to make corresponding changes to the tree visitor class (`HyperTalkTreeVisitor`).

## Using the IntelliJ IDE

This project will not compile in IntelliJ without first changing the GUI Designer settings.

The UI forms (window layouts) are generated using the GUI Designer built into IntelliJ's IDEA (Community Edition). Do not modify the generated source code by hand, as doing so will render those files incompatible with the GUI Designer tool.

By default, IntelliJ "hides" the generated code it creates inside of the `.class` files that it compiles. While this technique is quite elegant, it produces source code that is incomplete and which cannot be successfully built by other tools.

To correct this, you need to configure IntelliJ to generate its GUI boilerplate code in Java source:

1. From IntelliJ IDEA menu, choose "Preferences..."
2. Navigate to "Editor" -> GUI Designer
3. Select the "Java source code" option for GUI generation.
4. Apply the changes and "Rebuild project" from the "Build" menu.

## Thread auditing

Java's Swing (UI) libraries are single threaded and require that all calls made to them execute on the Swing dispatch thread. Unfortunately, unlike, say, Android or iOS which have similar requirements, Swing does not complain if you violate this contract; you simply wind up with difficult-to-debug, _well-it-works-most-of-the-time_ race conditions.

WyldCard includes an AspectJ (AOP) based thread auditing tool that will display a stack trace anytime it detects a method annotated with `@RunOnDispatch` as executing on a worker thread. This auditor is disabled by default. To enable it, run the app using Gradle with the `threadAudit` property defined:

```
$ gradle run -PthreadAudit
```

If everything goes according to plan, you should see the `:weaveClasses` task execute in the Gradle output.

## Frequently encountered problems

#### 1. Various classes in the `com.defano.hypertalk.parser` package don't exist. This project won't compile!

See the note at the top of this page. WyldCard makes use of generated Java sources created by Antlr4 (a parser generator tool). If you're missing classes in this package, you'll need to run the `gradle generateGrammarSource` task to recreate them.

#### 2. I imported this project into IntelliJ, but I get a weird `Duplicate method name "$$$getFont$$$"` error when I attempt to run it. What gives?

See the [section above](#using-the-intellij-ide): IntelliJ, by default, attempts to compile window layouts directly into binary (`.class` files). This project must be configured to translate these forms into Java source code (for portability to other IDEs and build environments).

#### That's all folks...
