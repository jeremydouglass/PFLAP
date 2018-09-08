<h1 align="center">
  <a href="https://github.com/micycle1/PFLAP">
  <img src="assets/icon.png" alt="Xi Editor"/></a><br>
PFLAP
</h1>

<p align="center"><em>Processing Formal Languages & Automata Package</em></p>

---

**PFLAP** is an interactive graphical tool for constructing and simulating automata machines (representations of formal languages), and is my attempt to create a [JFLAP](http://www.jflap.org/) alternative using the [Processing](https://processing.org/) library as the graphics backend. I do not intend to implement anywhere near the full functionality present in JFLAP.

## Latest Release

:inbox_tray: Download - [todo.jar](https://github.com/micycle1/PFLAP/releases/_.jar)

## Features
* Simulate Deterministic Finite Automata (DFA)
* Simulate Pushdown Automata (DPA)
* Stepping simulation mode
* Export to .png
* ' ' (space) substitution for λ.
* Undo/Redo
* Colour-customisation of all graphics

## Design Decisions
* **Deterministic-Machine Lambda Transitions.** λ-transitions in deterministic machines are allowed. Upon running the machine on an input, a λ transition will only be taken provided there is an input symbol to exchange and this symbol has no defined transition at the current state.
* **Java AWT Menubar.** Due to constraints with embedding a Processing surface within JavaFX or Swing stage, the older and displaced Java AWT library has been chosen for application-level GUI (the menubar). GUI elements within the surface are home brew using Procesing, or from the controlP5 library.

## Screenshot
<h1 align="center">
<img src="/assets/screen.PNG"/>
</h1>

## Libraries
- `processing` [github](https://github.com/processing/processing)
- `controlP5` [github](https://github.com/sojamo/controlp5)
- `dashedlines` [github](https://github.com/garciadelcastillo/-dashed-lines-for-processing-)
- `guava` [github](https://github.com/google/guava)
