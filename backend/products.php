<?php
require "config.php";

$base = "http://" . $_SERVER["HTTP_HOST"] . "/jewelry_api/";
$rows = $pdo->query("SELECT * FROM products ORDER BY id DESC")->fetchAll();

foreach ($rows as &$r) {
    $imgFile = __DIR__ . "/" . $r["image_url"];
    $tryFile = __DIR__ . "/" . $r["tryon_image"];
    $v1 = file_exists($imgFile) ? filemtime($imgFile) : 0;
    $v2 = file_exists($tryFile) ? filemtime($tryFile) : 0;

    $r["image_url"]   = $base . $r["image_url"] . "?v=" . $v1;
    $r["tryon_image"] = $base . $r["tryon_image"] . "?v=" . $v2;
}
echo json_encode(["success" => true, "products" => $rows]);