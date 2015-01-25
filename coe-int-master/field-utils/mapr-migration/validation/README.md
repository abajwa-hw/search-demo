Validation between clusters, relies on two components:

- A file listing from the source cluster.
- A file listing from the target cluster.

The file listing the contents of each cluster is then transformed to provide a comparable entity between the clusters.

The two transformed files are then compared to each other via unix 'diff'.  The results of that process