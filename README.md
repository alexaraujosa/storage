# Distributed Systems - In-Memory Key-Value Storage System

## Description  
This project implements a **distributed in-memory key-value storage service** where clients interact with a remote server via TCP sockets. The system supports concurrent user access, atomic operations, and advanced features like conditional reads. Designed for scalability and low contention, it prioritizes efficient thread management and protocol design to handle high-throughput workloads.

### 🎯 Purpose:  
The goal is to master **distributed systems fundamentals** by building a robust client-server architecture with emphasis on:  
- Concurrency control using Java threads  
- TCP socket communication with binary protocols  
- Atomicity guarantees for compound operations  
- Performance optimization under resource constraints  
- Client-server interaction patterns  

### 🚀 Key Features:  
- **User Authentication**: Secure registration/login via username/password.  
- **Key-Value Operations**:  
  - Basic: `put(key, value)`, `get(key)`  
  - Compound: Atomic `multiPut(pairs)` and `multiGet(keys)`  
- **Concurrency Limits**: Configurable max concurrent sessions (parameter `S`).  
- **Advanced Functionality**:  
  - **Multi-Threaded Clients**: Concurrent requests from a single client.  
  - **Conditional Reads**: `getWhen(key, keyCond, valueCond)` blocks until condition met.  
- **Benchmarking**: Inspired by YCSB for scalability testing under varying workloads.  

## 📚 Learning Outcomes  
- **Concurrency Management**: Implemented thread pools and synchronized blocks to minimize contention.  
- **Protocol Design**: Created a custom binary protocol using `DataInputStream`/`DataOutputStream`.  
- **Atomic Operations**: Ensured atomicity for compound writes via lock striping.  
- **Performance Tuning**: Analyzed throughput vs. thread count using YCSB-like benchmarks.  
- **Distributed Debugging**: Diagnosed race conditions and socket bottlenecks.  

## 🚧 Areas for Improvement  
- **Thread Quantity**: The number of threads that were created could be less.
- **Delegating tasks**: The work done by each thread could be better defined, also improving thread starvation.

## 👨‍💻 Contributors
- **Alex Araújo Sá** - [Alex Sá](https://github.com/alexaraujosa)
- **Paulo Alexandre Rodrigues Ferreira** - [Paulo](https://github.com/pauloarf)
- **Rafael Santos Fernandes** - [DarkenLM](https://github.com/DarkenLM)
- **José Vasconcelos** - [José Vasconcelos](https://github.com/josevasconcelos2002)

## 🛠️ Technologies Used  
- **Language**: Java 17+  
- **Concurrency**: `java.util.concurrent`, thread pools  
- **Networking**: TCP sockets
- **Benchmarking**: Custom YCSB-inspired client for stress testing
