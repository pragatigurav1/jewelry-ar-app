<?php
require "config.php";

$email = trim($_POST["email"] ?? "");
$pass  = $_POST["password"] ?? "";

if (!filter_var($email, FILTER_VALIDATE_EMAIL) || strlen($pass) < 6) {
    echo json_encode(["success" => false, "message" => "Enter a valid email and a password of 6+ characters"]);
    exit;
}

$stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
$stmt->execute([$email]);
if (!$stmt->fetch()) {
    echo json_encode(["success" => false, "message" => "No account found with this email"]);
    exit;
}

$hash = password_hash($pass, PASSWORD_DEFAULT);
$stmt = $pdo->prepare("UPDATE users SET password_hash = ? WHERE email = ?");
$stmt->execute([$hash, $email]);

echo json_encode(["success" => true, "message" => "Password updated. Please login."]);