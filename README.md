## Submission question

**What was the hardest design decision you made while building this, and what trade-off did you accept?**

The hardest part for me was designing the settlement: how to split an expense, and what to do when there is a leftover.

I was confused about how to calculate the settlement. In the end I used the net balance of each person (what others owe them minus what they owe others), then matched people who owe with people who should get money back.

For splitting, I round each share down (100.00 / 3 = 33.33). The leftover cent is borne by the payer, so the balances always add up to exactly 0.

**Trade-off:** the payer can lose a few cents per expense. I accepted it because it is tiny and the rule is simple.