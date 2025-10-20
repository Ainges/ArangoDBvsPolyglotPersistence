# Mini Social Network Dataset

This repository file contains the JSON data files and import commands for ArangoDB, Neo4j, MongoDB, and Redis. Save each section below as a separate `.json` or `.cypher` or `.js` file, then load into your containers.

# Import Instructions

## ArangoDB
1. Start the `arangodb` container.
2. Create the required collections MANUALLY (via the ArangoDB web interface at http://localhost:8529 or with `arangosh`):
   - Document collections: `users`, `posts`, `sessions`
   - Edge collections: `friendships`, `interactions`
   (Tip: if you prefer the command line, in `arangosh` you can run `db._create('users')` or `db._createEdgeCollection('friendships')`.)
3. Import the JSON files in the correct collection under Contents > Choose File > Import JSON.

## Neo4j
1. Start Neo4j container.
2. Use Cypher shell (http://localhost:7474/browser/)
   ```cypher
    // 1. Minimalen User-Knoten anlegen (nur id)
    UNWIND ["alice","bob","charlie","diana","eve","frank"] AS uid
    MERGE (:User {id: uid});
    
    // 2. Freundschafts-Beziehungen
    UNWIND [
    {from:"alice",to:"bob"},
    {from:"alice",to:"charlie"},
    {from:"bob",to:"eve"},
    {from:"bob",to:"diana"},
    {from:"charlie",to:"alice"},
    {from:"charlie",to:"frank"},
    {from:"diana",to:"frank"},
    {from:"eve",to:"alice"},
    {from:"eve",to:"bob"},
    {from:"frank",to:"diana"}
    ] AS f
    MATCH (a:User{id:f.from}), (b:User{id:f.to})
    MERGE (a)-[:FRIENDS_WITH]->(b);
    
    // 3. Like-Beziehungen zwischen User und Post
    // Post-Knoten ebenfalls nur mit id, wenn nötig
    UNWIND ["p1","p2","p3","p4","p5"] AS pid
    MERGE (:Post {id: pid});
    
    UNWIND [
    {user:"bob",     post:"p1"},
    {user:"charlie", post:"p1"},
    {user:"alice",   post:"p2"},
    {user:"eve",     post:"p2"},
    {user:"frank",   post:"p5"}
    ] AS l
    MATCH (u:User{id:l.user}), (p:Post{id:l.post})
    MERGE (u)-[:LIKED]->(p);
   ```

## MongoDB
```bash
mongoimport \
  --uri="mongodb://root:example@localhost:27017/admin" \
  --collection=users \
  --file=users.json \
  --jsonArray \
  --mode=upsert
```
```bash
mongoimport \
  --uri="mongodb://root:example@localhost:27017/admin" \
  --collection=posts \
  --file=posts.json \
  --jsonArray \
  --mode=upsert
# etc.
```

## Redis
```bash
cd ../PolyglotPersistence/
```

```bash
bash load_sessions.sh
```
