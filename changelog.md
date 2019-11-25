- Farseek structure generators now use world's own chunk generator for structure-placement chunk generation, instead of creating a copy. This should improve Streams performance and mod compatibility, but may break some edge cases (please report).
- As a result of the above, possibly fixed infinite Streams generation with certain combinations of mods (to be confirmed). Relevant issues include
    [Farseek #44](https://github.com/delvr/Farseek/issues/44),
    [Farseek #48](https://github.com/delvr/Farseek/issues/48),
    [Farseek #51](https://github.com/delvr/Farseek/issues/51),
    [Streams #74](https://github.com/delvr/Streams/issues/74),
    [Streams #78](https://github.com/delvr/Streams/issues/78),
    [Streams #83](https://github.com/delvr/Streams/issues/83),
    [Streams #86](https://github.com/delvr/Streams/issues/85),
    [Streams #86](https://github.com/delvr/Streams/issues/86), and
    [Streams #90](https://github.com/delvr/Streams/issues/90).
- As a consequence of the above, had to drop support for OpenTerrainGenerator versions earlier than v8 
  (game won't crash with older versions, but no Farseek features such as Streams will generate in those).
- Fixed [Streams crash with SpongeForge 7.1.7 and newer](https://github.com/delvr/Farseek/issues/49).
