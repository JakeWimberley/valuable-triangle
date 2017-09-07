# valuable-triangle

Valuable Triangle is a game in which one player gives clues about a series of
six subjects to another player, while that player tries to guess the subject. A
third person may participate as the "judge" who scores and determines if an
illegal clue is given. The gameplay is identical to the bonus round of a
popular television game show which first aired in the U.S. in the 1970s, and is
now played around the world.

Valuable Triangle was built with Clojure and the Quil graphics library.

## Usage

lein run

Press 'a' to move beyond the splash screen.
When players are ready to begin press 's' and the timer will start.
During the game, the clue-giver or judge presses these keys:
'c' for a correct answer
'p' if the players choose to pass, i.e., move to the next subject and return to the passed subject at the end
'b' to "buzz" an illegal clue. Rules have varied from version to version of the TV game, but generally hand gestures are not permitted, and clues must be a list in which each item ends in a noun (e.g. "an exciting game" versus "a game that excites you").

## License

Copyright © 2017 Jake Wimberley

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
