TG
==

Trident Genesis (TG) is a software development technology that implements the RESTful Objects architectural pattern. This is an ongoing effort to develop a software technology for constructing reliable and evolvable information systems, led by a bunch of dedicated individuals from Ukraine and Australia with a financial support of an Australian ISV [Fielden Management Services Pty Ltd](https://www.fielden.com.au). Fielden specialises in highly customisable domain-specific Enterprise Asset Managemen systems and uses TG as the foundation for developing several software product lines.

In essenсe, TG incapsulates **accidental complexity** in order to enable application developers to better harness the **essential complexity** that stems from the target business domain.

We've built TG for our own purpose to remove many low level technical obstacles (accidental complexity) that are often associated with building sophisticated transactional applications such as Enterprise Asset Maintenance (EAM) or Enterprise Resource Planning (ERP) systems. And we have successfully used TG for developing large-scale and mission-critical enterprise applications.

We look at the system design as the decomposition problem, which can be solved successfully with the right approach in the right context. TG establishes a well defined pattern for modelling business domains (dubbed RESTful Objects), including domain entities, processes and their interdependencies (down to the level of properties with automatic revalidation support). At the code level the description of the domain model is achieved by using a small set of base classes and various annotations. Together these constitute what we refer to as "Entity  Definition Language" (EDL), which is complemented by "Entity Query Language" (EQL) for data querying (relational databases).

Both EDL and EQL form the foundation of TG -- the rest is built on top of it. We've used this foundation to build Swing-based applications at first. And now the same foundation is used for delivering Progressive Web Applications and GraphQL-based Web API. Such seamless transition was a testimony for ourselves that the foundation of TG is indeed robust and such that can successfully evolve to fit the ever-changing landscape of surrounding technologies.

The notion of using of a single programming language for building TG-based applications was very important to us. That is why TG includes a domain-specific language for defining Web UI in Java. The business model is at the core of Web UI where editors and actions are derived based on the type information that is captured by the model. So, it is very much declarative. A quick illustration of the Web UI that can be produced this way is provided below.

<p align="center">
  <img align="center" src ="https://github.com/fieldenms/tg/blob/develop/platform-doc/doc/tg-overview/images/00-collage.png" alt="Demo App collage"/>
</p>

Documentation and Demo applications
-----------------------------------
One of the very important aspect of any technology is the documentation and demo applications that could be used for learning. Currently, all our applications are propriatery and their source cannot be shared.

The documentation forms part of this repository (LaTeX documents in `platform-doc`such as [TG Overview](https://fieldenmgmt.sharepoint.com/:b:/g/EWSGJEW5Do5EpYi_PmuOuB4B6AbTpBa-6EaN5gW3rWndlQ?e=TEtd37) and Wiki pages). However, these are mainly the overview-type documents and design sketches. A more training-oriented documentation is in the works and will be inlined with a demo business domain for building a smaller-size demo application.

Looking into the future
-----------------------
We're at a very early stage of open-sourcing the TG platform and there was a lot of deliberation if this is even necessary. However, we feel that our needs are not unique and that our effort can be helpful to others out there. We owe it to the vibrant open-source community who worked hard on their projects and libraries that we use in our day-to-day work in developing the TG platform.

There are very exciting plans regarding the future TG development directions, which include, but are not limited to advanced static typing of the domain models, asynchronous EQL execution engine and self-tuning, real-time messaging and on and on. And we look forward to close collaboration with some of you in the future!
