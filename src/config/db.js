const { Pool } = require('pg');

const pool = new Pool({
    user: 'postgres',
    host: '127.0.0.1',
    database: 'shm_market',
    password: '2005',
    port: 5432,
});

pool.on('error', (err, client) => {
    process.exit(-1);
});

module.exports = pool;