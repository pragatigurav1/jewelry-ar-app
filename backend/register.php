<?php
require "config.php";

$name  = trim($_POST["name"] ?? "");
$email = trim($_POST["email"] ?? "");
$pass  = $_POST["password"] ?? "";

if ($name === "" || !filter_var($email, FILTER_VALIDATE_EMAIL) || strlen($pass) < 6) {
    echo json_encode(["success" => false, "message" => "Enter valid name, email and password (min 6 chars)"]);
    exit;
}

$stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
$stmt->execute([$email]);
if ($stmt->fetch()) {
    echo json_encode(["success" => false, "message" => "Email already registered"]);
    exit;
}

$hash = password_hash($pass, PASSWORD_DEFAULT);
$stmt = $pdo->prepare("INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)");
$stmt->execute([$name, $email, $hash]);

echo json_encode(["success" => true, "message" => "Registration successful"]);