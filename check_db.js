const mysql = require('mysql2');

async function main() {
    const conn = mysql.createConnection({
        host: 'localhost',
        user: 'root',
        password: '123456',
        database: 'road_bike_rental'
    });
    
    conn.connect();
    
    // Check all admins
    const [admins] = await conn.query('SELECT * FROM sys_admin');
    console.log('Admins:', JSON.stringify(admins, null, 2));
    
    // Delete old 'admin' if exists, keep 'admin01'
    if (admins.some(a => a.username === 'admin')) {
        await conn.query('DELETE FROM sys_admin WHERE username = "admin"');
        console.log('Deleted old admin account');
    }
    
    conn.end();
}

main().catch(console.error);
