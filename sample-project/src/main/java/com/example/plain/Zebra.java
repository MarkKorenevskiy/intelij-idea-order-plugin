package com.example.plain;

import com.example.annotations.Order;

/** No @Order anywhere in this package -> default ordering. */
@Order(1)
public class Zebra {}
