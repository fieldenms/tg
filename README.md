TG
==

Trident Genesis (TG) is a software development technology, which has been developed by a bunch of dedicated individuals from Ukraine and Australia with a financial support of Fielden Management Services Pty. Ltd.

We've built TG for our own purpose in order to remove many low level technical obstacles that are often associated with building sophisticated transactional applications such as Enterprise Asset Maintenance (EAM) or Enterprise Resource Planning (ERP) systems. And we have successfully used TG for developing large-scale and mission-critical enterprise applications.

We look at the system design as a decomposition problem, which can be solved successfully with the right approach in the right context. TG establishes a well defined pattern for modelling the business domain, including domain entities, processes and their interdependencies (down to the level of properties with automatic revalidation support). At the code level the description of the domain model is achieved by using a small set of base classes and various annotations. Together these constitute what we refer to as "Entity  Definition Language" (EDL), which is complemented by "Entity Query Language" (EQL) for data querying. 

Both EDL and EQL form the foundation of TG -- the rest is built on top it. We've used this foundation to build Swing-based applications at first. Now the same foundation is used now for delivering responsive Polymer-based HTML5 single-page applications and GraphQL-based Web API.

The notion of using of a single programming language for building TG-based applications was very important to us. That is why TG includes a domain-specific language for defining Web UI in Java. The business model is at the core of Web UI where editors and actions are derived based on the type information that is captured by the model. So, it is very much declarative.

