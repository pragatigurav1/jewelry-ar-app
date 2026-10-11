<?php
require "config.php";

$email = trim($_POST["email"] ?? "");
$pass  = $_POST["password"] ?? "";

$stmt = $pdo->prepare("SELECT id, name, email, password_hash FROM users WHERE email = ?");
$stmt->execute([$email]);
$user = $stmt->fetch();

if ($user && password_verify($pass, $user["password_hash"])) {
    unset($user["password_hash"]);
    echo json_encode(["success" => true, "message" => "Login successful", "user" => $user]);
} else {
    echo json_encode(["success" => false, "message" => "Invalid email or password"]);
}