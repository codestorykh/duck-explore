# Process Flow
HTTP Request 
   │
   ▼
[RetryController] (Validates DTO via @Valid)
   │
   ▼
[DepositOrchestrator Proxy] (@Retryable intercepts here)
   │
   ├── Attempt 1 ──► [AccountTransactionalWorker] (Runs DB Transaction)
   │                 └── Throws CannotAcquireLockException?
   │
   ├── (Backoff 100ms)
   │
   ├── Attempt 2 ──► [AccountTransactionalWorker] (Runs fresh DB Transaction)
   │                 └── Success! (Returns response)
   │
   └── (If Attempt 3 fails) ──► recover(...) method is triggered

## The Lifecycle of an Idle Connection
[New Request Arrives]
│
▼
Is there an IDLE connection in the pool?
├── YES ──► Borrow it ──► Status: LEASED (Active) ──► Send payload & read response
│                                                            │
│                                                            ▼
│                                                    Release back to pool
│                                                            │
│                                                            ▼
└── NO  ──► Create new TCP socket (if below pool max) ──► Status: IDLE (Available)

## What Appears in Your Logs When Exhausted
================ RETRY TIMING SUMMARY ================
Status: EXHAUSTED / FAILED (Total Attempts: 3)
- Attempt #1 execution/connection wait: 3.004 s
  ↳ Backoff wait before attempt #2: 0.512 s
- Attempt #2 execution/connection wait: 3.002 s
  ↳ Backoff wait before attempt #3: 1.045 s
- Attempt #3 execution/connection wait: 3.001 s
------------------------------------------------------
Sum of Connection/Call Waits: 9.007 s
Sum of Backoff Waits:         1.557 s
TOTAL ELAPSED TIME:           10.564 s
======================================================