# StringAligner
This is a  Java program I wrote in my Bioinformatics class at the University of Maryland. It deals with the pairwise alignment of two strings. In this assignment, I only had to deal with global alignment and fitting alignment. 

Global alignment requires an end-to-end alignment of string x (the query), with the string y (the reference). 

![image](https://user-images.githubusercontent.com/49917374/182275909-0ec85a1b-6941-4864-9509-f6c71b497f1c.png)

Fitting alignment allows gaps for free, as long as the gaps occur before the start of string x & following the end of string x. In this case, we find the optimal alignment where we are trying to "fit" one string somewhere into the middle of another.

![image](https://user-images.githubusercontent.com/49917374/182275857-fad8cbe4-cb8b-4c96-8848-16e58dab6247.png)

In the above images, the red zones are where gaps or mismatches would occur. 

To give an example of what exactly the code does, lets pretend an input file is given as the first argument on the command line. These are its contents:
```
p-0
AAGATGCATATTTACACGAGCATCTCCATGTGGTGCGTTATCACGGCAACCTACGCGATTAGCCGACCGAGTAATGAGGTAGACTAGTATGCGCT
TTATGCCGCGGTCACCACAAAGATGCATATTAACACGCGCATTTCCATATGGTGCGTTATCCCGGCAACCTACGCGATTTCGACCGAGTAATGAGGTAGACTAGTATGCGCT
```

Treat the top string as string x (the query), and the bottom string as y (the reference). On the command line you must specify whether you want to do fitting or global alignment by including the word "fitting" or "global" as the second argument. If we were to run the command with the "fitting" argument, with the mismatch penalty being 1 & the gap penalty being 3, we would get the following output contained in another file: 

```
-22	19	112	12=1X5=1X4=1X5=1X12=1X17=2I1X32=
```

The first number, -22, represents the total penalty gathered from each gap + mismatch that occurred in the optimal alignment. This means, with a mismatch penalty of 1 &  a gap penalty of 3, there is no alignment that will give a better score than -22. The next two numbers, 19 and 112, represent the indices in string y where the optimal alignment starts and ends. Intuitively, if you look at the 19th index of string y, you can see the next 12 characters all align perfectly with the first 12 characters in string x. The final argument in the output file is a CIGAR string that shows how to transform string y into x. It represents a list of pairs of (count, operation) where count is an integer >= 1 and operation explains what operation follows in the alignment. In this case, the pairs of operations in this CIGAR string would look like (12, =), (1, X), (5, =), (1, X), etc. 

The following specific characters in the CIGAR string (=, X, D, I) are explained below. 

```=``` - in the alignment the corresponding nucleotides in X and Y match
```X``` - in the alignment the corresponding nucleotides in X and Y mismatch
```D``` - in the alignment the next nucleotide is deleted from the reference (i.e. deleted from Y, which is equivalent to having this nucleotide inserted into X)
```I``` - in the alignment the next nucleotide is interted into the reference (i.e. inserted into Y, which is equivalent to having this nucleotide deleted from X)

So in this case, the alignment starts off with 12 matching characters at index 19, then 1 mismatch between characters in string x and y, then another 5 matching characters, so on and so forth. 

**TL;DR** The CIGAR string is a simple representation of how to transform string y into x.

The input file is expected to have many inputs of pairs of strings, but the format for each input must hold as the example given above, so three alignment inputs would have to look something like this in the input:
```
p-0
CTAATATGACCTAGATACTAGATCCCCACATGGACCGAAAGCCACGTATATGTTTGCGTAAATTCTGCTACGTCAATAGGC
CTAATATGACCTAGATACTAGATCCCCACATGGACCGAAAGCCAAGTATATTAAATTCTGCTACGTTCGTGTGCAATAGGC
p-1
CCATGCGGTGGTGACCAAATGCTGTAGGTTTAAGTTTGTTCCCTACGAGCGAATTTGTCTGATGAGCCTAAGTCTATTAAGGGACGAACCAGCTAATCG
CCAAGCGGTGGTGACCAAATGCTGTAGGTTTAAGTTTGTTCCCTACGAGCGAATTTGTCTGATAAGCCTAAGTTTATTAAGGGACGAACCAGCTAATCT
p-2
ACCGAAGGTTTCACATGGCCAGTCGTATCTGTTATACATTCAGTGGTCTTGCCGTGCTTTTTCCGTCGCTATGTGGTCGTGCGTTGCCTTCTTCC
ACCGAAGGTGTCACATGGCCAGTCGTATCTATACATAGTATCAGAGGTCTTGCCGTGCTTGTTCCGTCTCTATGTGGTCGTGCGTTGCCTTCTTCC
```
