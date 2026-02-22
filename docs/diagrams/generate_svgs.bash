# Uses the plantuml docker image to generate svgs from .pumls within this dir
docker run --rm -v "$PWD":/work -w /work plantuml/plantuml -tsvg *.puml