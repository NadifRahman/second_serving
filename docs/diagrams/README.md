All project diagrams should live within this directory. This project uses plantuml as the "diagraming engine".

Plantuml diagrams use the `.puml` file extension. It is recommended to use some kind of plantuml plugin/extension in 
your IDE of choice to create/edit these diagram files. 

Note that GitHub does not natively support plantuml within markdown files (unlike Mermaid), and the author of this 
project likes plantuml over Mermaid...so our workaround is to generate `.svg`s using [a script](generate_svgs.bash). 
Anytime you create/edit a `.puml` file for this project, run the mentioned script to generate the `.svg`. It can be then
be used in markdown files. 
---
Checkout the [PlantUML docs](https://plantuml.com/) for more information.