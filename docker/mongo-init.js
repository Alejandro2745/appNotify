rs.initiate({
  _id: "rs0",
  members: [{ _id: 0, host: "mongodb:27017" }]
});

const adminDb = db.getSiblingDB('admin');
adminDb.createUser({
  user: 'appnotify',
  pwd: 'appnotify',
  roles: [{ role: 'root', db: 'admin' }]
});

const appDb = db.getSiblingDB('appnotify');
appDb.createUser({
  user: 'appnotify',
  pwd: 'appnotify',
  roles: [{ role: 'readWrite', db: 'appnotify' }]
});