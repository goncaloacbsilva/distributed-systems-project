# Turmas

Distributed Systems Project 2021/2022

## Authors

**Group T40**

### Team Members


| Number | Name             | User                             | Email                               |
|---|------------------|----------------------------------|-------------------------------------|
| 96925 | Gonçalo Silva    | <https://github.com/goncaloacbsilva>   | <mailto:goncalo.c.brito.da.silva@tecnico.ulisboa.pt>   |
| 93712 | Gonçalo Ferreira | <https://github.com/ist193712>     | <mailto:bob@tecnico.ulisboa.pt>     |
| 93733 | Manuel Carneiro  | <https://github.com/ManuMaquina> | <mailto:charlie@tecnico.ulisboa.pt> |

## Getting Started

The overall system is made up of several modules. The main server is the _ClassServer_. The clients are the _Student_,
the _Professor_ and the _Admin_. The definition of messages and services is in the _Contract_. The future naming server
is the _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/Turmas) or a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too, just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
