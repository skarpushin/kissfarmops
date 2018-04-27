# KISSFarmOps
K.I.S.S. Farm Operations - a very affordable way to manage simple web farm

# What is it for
It's a tool for managing number of applications running on machines in a cloud.
Typical goals are:
* See the dashboard with information about nodes and applications
* Easily scale-out nodes
* Run a rolling update process
* Notify me if important event happens

# Not an enterprise-grade solution
In case you're looking for enterprise grade solution, you must consider Kubernetes or DC/OS first.
I decided to create `KISSFarmOps` because I needed something:
* Very simple to learn
* Re-use existing knowledge as much as possible
* Easy to setup
* Resource savvy (don't require a lot of RAM for example)
* Lock-in free (be as non-biased as possible)

I spent couple weeks learning Kubernetes (and DC/OS) and found it to be a great
enterprise-level orchestration solution but it's shortage on aforementioned
criteria gave me an idea to come up with other soluton.

I spent another couple days trying to find existing, but found nothing.

Since I see this piece of software as rather simple I decided to invest couple
months of my spare time and build one from scratch.
