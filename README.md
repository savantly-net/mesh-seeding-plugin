# Mesh Seeding Plugin


## Quick start

Download the latest release [mesh-seeding-plugin-x.x.x.jar](https://github.com/savantly-net/mesh-seeding-plugin/releases)

Put the jar file in your mesh plugins folder.  
When mesh starts up, an empty plugin configuration file will be generated.  

## Configuration 

This is an example of a configuration file that creates a project, microschema, schema, nodes, groups, roles, and associates groups to roles. 
The configuration file is parsed when mesh starts and the plugin is initialized.  

```
meshScripts:
  test1:
    projectName: example
    microSchemaFiles:
      7b28ec90585140a8a8ec905851b0a8ec: "schemas/example_microschema.json"
    schemaFiles:
      5bdb2a9db6594a8e9b2a9db6593a8ef1: "schemas/example.json"
    nodeFiles:
      b60a50dbe8354acd8a50dbe835bacd09: "nodes/example_node1.json"
      a59ac0b6338f42c69ac0b6338f22c646: "nodes/example_node2.json"
      73aeb52bc5344345aeb52bc5345345c1: "nodes/example_node3.json"
    roles:
      - editor
      - contributor
    groups:
      - editor
      - contributor
    rolesToGroups:
      editor: editor
      contributor: contributor
```
 
If an artifact already exists, it passes over it.  
The 'keys' in the json fields represent the IDs that will be assigned to the given artifact.  
For example, `7b28ec90585140a8a8ec905851b0a8ec: "schemas/example_microschema.json"` will generate the microschema with the id of `7b28ec90585140a8a8ec905851b0a8ec`.  

Since the IDs are provided manually - they can be referenced from inside the json schema/node json docs.  

See the test resources for more examples -  
[src/test/resources](src/test/resources)   


## Development  

The plugin could be improved by replacing the blocking calls with non-blocking calls.  
Also, an enhancement might be to allows nested configuration structures to more easily identify dependencies.  

Feel free to create PRs!  