# Binary Frequency Dictionary Format

I was trying to create a smaller, faster to parse format for
frequency dictionaries which are used for spell checking algorithms.

I've managed a modest improvement in compression ratio, and a big
improvement in parsing speed, up to 80% faster!

```text
Binary Frequency Dictionary Compression
---------------------------------------

Speed:
------
 txt took 77ms to load
  gz took 83ms to load
fdic took 155ms to load
fdi2 took 20ms to load

fdic1 was 78ms (50.32%) slower
fdic2 was 57ms (74.03%) faster!

Compression:
------------
 txt size: 1330 KB
  gz size: 592 KB
fdi1 size: 656 KB
fdi2 size: 580 KB

FDIC1 was 49.38% of the original size
a reduction of 50.62% (689440 B)

FDIC2 was 43.65% of the original size
a reduction of 56.35% (767576 B)
```

It's larger than just GZIPing the whole thing, and its _MUCH_ slower to load.