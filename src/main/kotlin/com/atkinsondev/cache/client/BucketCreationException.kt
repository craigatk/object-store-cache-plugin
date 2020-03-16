package com.atkinsondev.cache.client

import java.lang.RuntimeException

class BucketCreationException(message: String, cause: Throwable) : RuntimeException(message, cause)
