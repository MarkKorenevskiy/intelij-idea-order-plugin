package com.example.mixed;

import com.example.annotations.Order;
import com.example.constants.Priorities;

/** Constant expression: HIGHEST + 1 = -999 -> first. */
@Order(Priorities.HIGHEST + 1)
public class Delta {}
