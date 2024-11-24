# Binary Frequency Dictionary Format

I was trying to create a smaller, faster to parse format for
frequency dictionaries which are used for spell checking algorithms.

So far I've largely failed:

```text
Binary Frequency Dictionary Compression
---------------------------------------

Speed:
------
 txt took 63ms to load
  gz took 77ms to load
fdic took 145ms to load

fdic was 82ms (43.45%) slower 

Compression:
------------
 txt size: 1330 KB
  gz size: 592 KB
fdic size: 656 KB

fdic was 49.38% of the original size
a reduction of 50.62% (689440 B)
```

It's larger than just GZIPing the whole thing, and its _MUCH_ slower to load.