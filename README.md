TG
==

Trident Genesis (TG) is a software development technology, which has been developed by a bunch of dedicated individuals from Ukraine and Australia with a financial support of Fielden Management Services Pty. Ltd.

We've built TG for our own purpose in order to remove many low level technical obstacles that are often associated with building sophisticated transactional applications such as Enterprise Asset Maintenance (EAM) or Enterprise Resource Planning (ERP) systems. And we have successfully used TG for developing large-scale and mission-critical enterprise applications.

We look at the system design as the decomposition problem, which can be solved successfully with the right approach in the right context. TG establishes a well defined pattern for modelling business domains, including domain entities, processes and their interdependencies (down to the level of properties with automatic revalidation support). At the code level the description of the domain model is achieved by using a small set of base classes and various annotations. Together these constitute what we refer to as "Entity  Definition Language" (EDL), which is complemented by "Entity Query Language" (EQL) for data querying (raletaional databases).

Both EDL and EQL form the foundation of TG -- the rest is built on top it. We've used this foundation to build Swing-based applications at first. And now the same foundation is used for delivering responsive Polymer-based HTML5 single-page applications and GraphQL-based Web API. Such seamless transition was a testimony for ourselves that the foundation of TG is indeed robust and such that can successfully evolve to fit the ever-changing landscape of surrounding technologies.

The notion of using of a single programming language for building TG-based applications was very important to us. That is why TG includes a domain-specific language for defining Web UI in Java. The business model is at the core of Web UI where editors and actions are derived based on the type information that is captured by the model. So, it is very much declarative. A quick illustration of the Web UI that can be produced this way is provided below.

<p align="center">
  <img align="center" src ="https://github.com/fieldenms/tg/blob/develop/platform-doc/doc/tg-overview/images/00-collage.png" alt="Demo App collage"/>
</p>

Documentation and Demo applications
-----------------------------------
One of the very important aspect of any technology is the documentation and demo applications that could be used to learning. Currently, all our applications are propriatery.

The documentation forms part of this repository (LaTeX documents in `platform-doc`such as https://github.com/fieldenms/tg/blob/develop/platform-doc/doc/tg-overview/tg-overview.tex and Wiki pages). However, these are mainly the overview-type documents and design sketches. A more training-oriented documentation is in the works and will be inlined with a demo business domain for building that smaller-size demo application.

Looking into the future
-----------------------
We're at a very early stage of open-sourcing the TG platform. There are very exciting plans regarding the TG development directions, which included, but are not limited to advanced static typing of the domain models, asynchronous EQL execution engine and self-tuning, real-time messaging and on and on.
