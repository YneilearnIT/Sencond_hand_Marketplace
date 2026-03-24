const express = require('express');
const router = express.Router();

// Route tạm thời để kiểm tra server
router.get('/test', (req, res) => {
    res.json({ message: "Backend SHM Market đã sẵn sàng!" });
});

module.exports = router;