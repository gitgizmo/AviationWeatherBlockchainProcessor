Aviation Weather Blockchain Processor
=====================================

The Aviation Weather Blockchain is a data store with the mandate to store aviation weather in such a way that the data is non-reputable, the chronology of the adding to the data store is guaranteed, access to the data is performant and highly available.

Currently Aviation Weather Blockchain stores only METARs but may add support for TAFs and PIREPs in the future.

What is a METAR
---------------

METAR is an abbreviated weather report predominantly used by pilots for the purposes of flight planning. METAR is an abbreviation for Meteorological Terminal Aviation Routine

MEATRs follow a prescribed format and at first glance challenging to read. However, with a little practice it soon becomes second hand.

Here is an example of a METAR in raw text format.

**CYYZ 072100Z 11004KT 6SM BR BKN012 BKN034 02/00 A2968**

The above METAR is for the ICAO station identifier CYYZ for the 7th day of the month at 2100 Zulu. The winds are from 110 degrees at 4 knots. There is 6 statute miles of visibility, with light mist. The clouds cover is broken at 1,200 feet AGL and again at 3,400 feet AGL. The temperature is 2 degrees Celsius and the dewpoint is 0 degrees Celsius. The altimeter reading is 29.68. Often the METAR appends remarks denoted with RMK and contains other relevant abbreviated information.

https://en.wikipedia.org/wiki/METAR

What is the underlying technology
---------------------------------

[Multichain](https://github.com/MultiChain) blockchain underpins the solution. blockchain is most well-known for being the underlying technology behind Bitcoin and other cryptocurrencies. In the context of cryptocurrencies, blockchain serves as the ledger that records transactions that effectively assign and re-assign currency amounts to participating parties. Aviation Weather blockchain employs the ledger as a means to record and digitally sign content which in this case is METARs. blockchain itself hashes content, groups them together in blocks, and links the blocks together via hash codes. Simply stated, METARs are recorded in the blockchain and the blockchain guarantees the order the METARs were added and that they were not tampered with. To get all of this done Aviation Weather blockchain employs Multichain 2.0 blockchain technology.

Why use Blockchain
------------------

You may be asking yourself why would I use a blockchain when I can use a database like everyone else. Blockchains and databases are the same in that they both store data. The decision when to use each depends on what problems you are trying to solve. Databases tend to be controlled by a single authority and tend to dish out the data via an application. Amazon and Facebook are good examples of this type of architecture. Blockchains use a peer-to-peer style of storage. This means the data is replicated to any peer who wants it. You can add data to any peer, and it will be broadcast to other peers who cascade it to even more peers until all peers have all the data. In addition, it has the smarts to make sure nobody has tampered with it. All of the redundancy of this style of architecture means that there is no single point of failure, and no single authority to hold the data at ransom.

https://www.multichain.com/blog/2016/03/blockchains-vs-centralized-databases/

Now and the future
------------------

The most powerful usage of Aviation Weather Blockchain requires both content producers (weather observation stations) and content consumers (anybody who wants a METAR) to be completely distributed. This means that the individual weather stations would independently add the METAR data to the blockchain, and everybody can get a copy of the entire blockchain to query it. Access to creating content would of course be managed through permissions. For the time being, METAR are harvested from https://www.aviationweather.gov/ and added to the blockchain on an hourly basis. However, consumers are free to set up a node and obtain their own local copy of the blockchain. So, in the interim while the content production is not distributed, the consumption is.

Installation
------------
[AviationWeatherBlockchainProcessor installation instructions](https://github.com/gitgizmo/AviationWeatherBlockchainProcessor/blob/master/AviationWeatherBlockchainProcessor/installation.txt)

