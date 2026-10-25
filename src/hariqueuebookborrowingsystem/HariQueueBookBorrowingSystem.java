/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package hariqueuebookborrowingsystem;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Scanner;

public class HariQueueBookBorrowingSystem {
    private static final Scanner in = new Scanner(System.in);
    private static final String DIR = "library_data";
    private static final String BOOKS = DIR + "/books.csv";
    private static final String STUDENTS = DIR + "/students.csv";
    private static final String BORROWED = DIR + "/borrowed.csv";
    private static final String WAITING = DIR + "/waiting_queue.csv";

    private static BookList books = new BookList();
    private static StudentList students = new StudentList();
    private static BorrowList borrowed = new BorrowList();
    private static BorrowQueue queue = new BorrowQueue();

    public static void main(String[] args) {
        try {
            Files.createDirectories(Paths.get(DIR));
            loadData();
            seedIfEmpty();
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
            return;
        }
        int choice;
        do {
            clearConsole("");
            System.out.println("\n======================================");
            System.out.println("   HARI QUEUE BOOK BORROWING SYSTEM");
            System.out.println("======================================");
            System.out.println("\nMAIN MENU\n1. Student\n2. Librarian\n0. Exit");
            choice = readInt("Choose: ");
            switch (choice) {
                case 1: clearConsole(""); studentAccess(); break;
                case 2: librarianAccess(); break;
                case 0: saveSafely(); System.out.println("Data saved. Goodbye!"); break;
                default: System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    
    private static void studentAccess() {
        System.out.println("======================================");
        System.out.println("   STUDENT ACCESS");
        System.out.println("======================================");
        System.out.println("\n1. Register\n2. Sign in\n0. Back");

        int c = readInt("Choose: ");

        if (c == 1) {
            registerStudent();
        } else if (c == 2) {
            clearConsole("");

            Student s = null;
            int i = 1;

            while (i <= 3) {
                System.out.println("======================================");
                System.out.println("   STUDENT LOGIN");
                System.out.println("======================================");

                String id = readLine("Student ID: ")
                        .trim().toUpperCase();

                s = students.find(id);

                if (s != null) {
                    System.out.println("Login successful!");
                    System.out.println("Welcome, " + s.name + "!");
                    studentMenu(s);
                    return;
                }

                clearConsole("Invalid Student ID. [" + i + "/3]");
                i++;
            }

            System.out.println("Maximum attempts reached. Returning to menu.");
            studentAccess();
        }
    }

    private static void studentMenu(Student s) {
        int c;
        do {
            clearConsole("");
            System.out.println("======================================");
            System.out.println("   STUDENT MENU [" + s.name + "] (" + s.id + ") ---");
            System.out.println("======================================");
            System.out.println("1. View books\n2. Search books\n3. Borrow / join queue");
            System.out.println("4. Return book\n5. My borrowed books\n6. My queue requests\n0. Log out");
            c = readInt("Choose: ");
            switch (c) {
                case 1: books.display(); break;
                case 2: searchBooks(); break;
                case 3: borrow(s); break;
                case 4: returnBook(s); break;
                case 5: showBorrowed(s.id); break;
                case 6: queue.showForStudent(s.id); break;
                case 0: break;
                default: System.out.println("Invalid choice.");
            }
        } while (c != 0);
    }

    private static void librarianAccess() {
        System.out.println("\nDemo login: admin / admin123");
        String u = readLine("Username: ");
        String p = readLine("Password: ");
        if (!u.equals("admin") || !p.equals("admin123")) {
            System.out.println("Incorrect credentials.");
            return;
        }
        int c, c2;
        do {
            clearConsole("");
            System.out.println("======================================");
            System.out.println("   LIBRARIAN ACCESS");
            System.out.println("======================================");
            
            System.out.println("1. BOOKS MANAGEMENT\n2. STUDENT MANAGEMENT\n3. LOG OUT");
            c = readInt("Choose: ");
            
            if(c == 1){
                clearConsole("");
                System.out.println("======================================");
                System.out.println("   BOOK MANAGEMENT");
                System.out.println("======================================");
                System.out.println("1. View books\n2. Add book\n3. Edit book\n4. Delete book");
                System.out.println("5. Search books\n6. Borrowed books\n7. View waiting queue\n0. Back");
                c2 = readInt("Choose: ");
                
                switch (c2) {
                    case 1: books.display(); break;
                    case 2: addBook(); break;
                    case 3: editBook(); break;
                    case 4: deleteBook(); break;
                    case 5: searchBooks(); break;              
                    case 6: showBorrowed(null); break;
                    case 7: queue.display(); break;
                    case 0: c2 = 0; break;
                    default: System.out.println("Invalid choice.");
                }
            }
            else if(c == 2){
                clearConsole("");
                System.out.println("======================================");
                System.out.println("   STUDENT MANAGEMENT");
                System.out.println("======================================");
                System.out.println("1. Register student\n2. List students\n0. Back");
                c2 = readInt("Choose: ");
                switch (c2) {
                    case 1: registerStudent(); break;
                    case 2: students.display(); break;
                    case 0: c2 = 0; break;
                    default: System.out.println("Invalid choice.");
                }
            }
        } while (c != 0);
    }

    private static void addBook() {
        clearConsole("");
        System.out.println("======================================");
        System.out.println("   ADD BOOK");
        System.out.println("======================================");
        String id = readLine("Unique book ID: ").trim().toUpperCase();
        if (id.isEmpty() || books.find(id) != null) { System.out.println("Blank or duplicate ID."); return; }
        String title = readLine("Title: ").trim(), author = readLine("Author: ").trim();
        int copies = readInt("Total copies: ");
        if (title.isEmpty() || author.isEmpty() || copies < 0) {
            System.out.println("Title/author required; copies cannot be negative."); return;
        }
        books.add(new Book(id, title, author, copies, copies));
        saveSafely(); System.out.println("Book added.");
    }

    private static void editBook() {
        clearConsole("");
        System.out.println("======================================");
        System.out.println("   EDIT BOOK");
        System.out.println("======================================");
        String id = readLine("Book ID to edit: ").trim().toUpperCase();
        Book b = books.find(id);
        if (b == null) { System.out.println("Book not found."); return; }
        String title = readLine("New title (Enter to keep): ").trim();
        String author = readLine("New author (Enter to keep): ").trim();
        String count = readLine("New total copies (Enter to keep): ").trim();
        int total = b.total;
        if (!count.isEmpty()) {
            try { total = Integer.parseInt(count); }
            catch (NumberFormatException e) { System.out.println("Invalid number."); return; }
        }
        int borrowed = b.total - b.available;
        if (total < borrowed || total < 0) { System.out.println("Total cannot be below borrowed copies."); return; }
        if (!title.isEmpty()) b.title = title;
        if (!author.isEmpty()) b.author = author;
        b.total = total; b.available = total - borrowed;
        saveSafely(); System.out.println("Book updated.");
    }

    private static void deleteBook() {
        clearConsole("");
        System.out.println("======================================");
        System.out.println("   DELETE BOOK");
        System.out.println("======================================");
        String id = readLine("Book ID to delete: ").trim().toUpperCase();
        Book b = books.find(id);
        if (b == null) { System.out.println("Book not found."); return; }
        if (b.available != b.total || queue.containsBook(id)) {
            System.out.println("Cannot delete a book with active borrowed or waiting requests."); return;
        }
        books.remove(id); saveSafely(); System.out.println("Book deleted.");
    }

    private static void searchBooks() {
        clearConsole("");
        System.out.println("======================================");
        System.out.println("   SEARCH BOOK");
        System.out.println("======================================");
        String q = readLine("Search by ID, title, or author: ").trim().toLowerCase();
        if (q.isEmpty()) { System.out.println("Enter a search term."); return; }
        boolean found = false;
        for (Book b=books.head; b!=null; b=b.next) {
            if (b.id.toLowerCase().contains(q) || b.title.toLowerCase().contains(q)
                    || b.author.toLowerCase().contains(q)) {
                if (!found) bookHeader();
                printBook(b); found = true;
            }
        }
        if (!found) System.out.println("No matching books found.");
    }

    private static void registerStudent() {
        clearConsole("");
        System.out.println("======================================");
        System.out.println("   STUDENT REGISTRATION");
        System.out.println("======================================");
        String name = readLine("Student name: ").trim();
        if (name.isEmpty()) { System.out.println("Name is required."); return; }
        String id = students.nextId();
        students.add(new Student(id, name));
        saveSafely();
        clearConsole("");
        System.out.println("Registered! Your member ID is " + id + ". Keep it for future sign-in.");
        studentAccess();
    }

    private static void borrow(Student s) {
        clearConsole("");
        System.out.println("======================================");
        System.out.println("   BORROW BOOK");
        System.out.println("======================================");
        String id = readLine("Book ID: ").trim().toUpperCase();
        Book b = books.find(id);
        if (b == null) { System.out.println("Book not found."); return; }
        if (borrowed.has(s.id, id)) { System.out.println("You already borrowed this book."); return; }
        if (queue.contains(s.id, id)) { System.out.println("You are already waiting for this book."); return; }

        if (b.available > 0 && !queue.containsBook(id)) {
            issueBook(s, b);
        } else {
            queue.enqueue(new Request(s.id, id, LocalDate.now()));
            saveSafely();
            System.out.println("Added to waiting queue. Your position is " + queue.position(s.id, id) + ".");
        }
    }

    private static void issueBook(Student s, Book b) {
        clearConsole("");
        System.out.println("======================================");
        System.out.println("   BOOK LOGS");
        System.out.println("======================================");
        b.available--;
        LocalDate today = LocalDate.now();
        borrowed.add(new BorrowRecord(s.id, b.id, today));
        saveSafely();
        System.out.println("Borrow successful: " + b.title);
        System.out.println("Borrow date: " + today);
    }

    private static void returnBook(Student s) {
        clearConsole("");
        System.out.println("======================================");
        System.out.println("   RETURN BOOK");
        System.out.println("======================================");
        String id = readLine("Book ID to return: ").trim().toUpperCase();
        BorrowRecord rec = borrowed.find(s.id, id);
        if (rec == null) { System.out.println("You have not borrowed that book."); return; }
        Book b = books.find(id);
        borrowed.remove(s.id, id);
        if (b != null && b.available < b.total) b.available++;
        System.out.println("Book returned.");

        Request r = queue.removeFirstForBook(id);
        while (r != null) {
            Student waitingStudent = students.find(r.studentId);
            if (waitingStudent != null && !borrowed.has(r.studentId, id) && b != null && b.available > 0) {
                b.available--;
                LocalDate today = LocalDate.now();
                borrowed.add(new BorrowRecord(waitingStudent.id, id, today));
                System.out.println("Queue processed: " + waitingStudent.name + " (" + waitingStudent.id
                        + ") received the book.");
                break;
            } else {
                System.out.println("Skipped ineligible request from " + r.studentId + ".");
            }
            r = queue.removeFirstForBook(id);
        }
        saveSafely();
    }

    private static void showBorrowed(String studentId) {
        clearConsole("");
        System.out.println("======================================");
        System.out.println("   BORROWED BOOKS");
        System.out.println("======================================");
        boolean found = false;
        System.out.printf("%-10s %-10s %-25s %-12s%n", "Member ID", "Book ID", "Title", "Borrowed");
        for (BorrowRecord l=borrowed.head; l!=null; l=l.next) {
            if (studentId == null || l.studentId.equalsIgnoreCase(studentId)) {
                Book b = books.find(l.bookId);
                String title = b == null ? "(unknown book)" : b.title;
                System.out.printf("%-10s %-10s %-25s %-12s%n", l.studentId, l.bookId, clip(title,25), l.borrowed);
                found = true;
            }
        }
        if (!found) System.out.println("No borrowed books.");
    }

    private interface LineReader { void accept(String line); }
    private interface FileWriterAction { void write(PrintWriter out) throws IOException; }

    private static void loadData() throws IOException {
        readFile(BOOKS, line -> {
            String[] a=line.split(",",-1);
            if (a.length==5) try { books.add(new Book(dec(a[0]),dec(a[1]),dec(a[2]),Integer.parseInt(a[3]),Integer.parseInt(a[4]))); }
            catch (RuntimeException e) { System.out.println("Skipped invalid book record."); }
        });
        readFile(STUDENTS, line -> {
            String[] a=line.split(",",-1);
            if (a.length==2) try { students.add(new Student(dec(a[0]),dec(a[1]))); }
            catch (RuntimeException e) { System.out.println("Skipped invalid student record."); }
        });
        readFile(BORROWED, line -> {
            String[] a=line.split(",",-1);
            if (a.length>=3) try { borrowed.add(new BorrowRecord(dec(a[0]),dec(a[1]),LocalDate.parse(a[2]))); }
            catch (RuntimeException e) { System.out.println("Skipped invalid borrowed record."); }
        });
        readFile(WAITING, line -> {
            String[] a=line.split(",",-1);
            if (a.length==3) try { queue.enqueue(new Request(dec(a[0]),dec(a[1]),LocalDate.parse(a[2]))); }
            catch (RuntimeException e) { System.out.println("Skipped invalid queue record."); }
        });
    }

    private static void saveData() throws IOException {
        writeFile(BOOKS, out -> {
            for (Book b=books.head;b!=null;b=b.next)
                out.println(enc(b.id)+","+enc(b.title)+","+enc(b.author)+","+b.total+","+b.available);
        });
        writeFile(STUDENTS, out -> {
            for (Student s=students.head;s!=null;s=s.next)
                out.println(enc(s.id)+","+enc(s.name));
        });
        writeFile(BORROWED, out -> {
            for (BorrowRecord l=borrowed.head;l!=null;l=l.next)
                out.println(enc(l.studentId)+","+enc(l.bookId)+","+l.borrowed);
        });
        writeFile(WAITING, out -> {
            for (Request r=queue.front;r!=null;r=r.next)
                out.println(enc(r.studentId)+","+enc(r.bookId)+","+r.date);
        });
    }

    private static void readFile(String path, LineReader reader) throws IOException {
        Path p=Paths.get(path);
        if (!Files.exists(p)) return;
        for (String line:Files.readAllLines(p)) if (!line.trim().isEmpty()) reader.accept(line);
    }

    private static void writeFile(String path, FileWriterAction action) throws IOException {
        try (PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
            action.write(out);
            if (out.checkError()) throw new IOException("Failed writing " + path);
        }
    }

    private static void saveSafely() {
        try { saveData(); }
        catch (IOException e) { System.out.println("WARNING: save failed: " + e.getMessage()); }
    }

    private static void seedIfEmpty() {
        if (books.head == null) {
            books.add(new Book("B1001","Introduction to Java","Herbert Schildt",3,3));
            books.add(new Book("B1002","Data Structures and Algorithms","Goodrich",2,2));
            books.add(new Book("B1003","Clean Code","Robert C. Martin",1,1));
            books.add(new Book("B1004","Computer Networks","Andrew S. Tanenbaum",2,2));
        }
        if (students.head == null) students.add(new Student("2026S1000","Demo Student"));
        saveSafely();
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(in.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a valid whole number."); }
        }
    }
    private static String readLine(String prompt) { System.out.print(prompt); return in.nextLine(); }
    private static String clip(String s,int max) { return s.length()<=max?s:s.substring(0,max-3)+"..."; }
    private static void bookHeader() {
        System.out.printf("%-8s %-32s %-25s %-8s %-10s%n","Book ID","Title","Author","Total","Available");
    }
    private static void printBook(Book b) {
        System.out.printf("%-8s %-32s %-25s %-8d %-10d%n",b.id,clip(b.title,32),clip(b.author,25),b.total,b.available);
    }
    private static String enc(String s) {
        return java.util.Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    private static String dec(String s) {
        return new String(java.util.Base64.getDecoder().decode(s),java.nio.charset.StandardCharsets.UTF_8);
    }

    private static class Book {
        String id,title,author; int total,available; Book next;
        Book(String id,String title,String author,int total,int available) {
            this.id=id;this.title=title;this.author=author;this.total=total;this.available=available;
        }
    }
    private static class BookList {
        Book head,tail;
        Book find(String id) { for(Book b=head;b!=null;b=b.next) if(b.id.equalsIgnoreCase(id)) return b; return null; }
        void add(Book b) { if(head==null)head=tail=b;else{tail.next=b;tail=b;} }
        void remove(String id) {
            Book prev=null,cur=head;
            while(cur!=null&&!cur.id.equalsIgnoreCase(id)){prev=cur;cur=cur.next;}
            if(cur==null)return;
            if(prev==null)head=cur.next;else prev.next=cur.next;
            if(tail==cur)tail=prev;
        }
        void display() {
            if(head==null){System.out.println("No books.");return;}
            bookHeader();for(Book b=head;b!=null;b=b.next)printBook(b);
        }
    }

    private static class Student {
        String id,name; Student next;
        Student(String id,String name){this.id=id;this.name=name;}
    }
    private static class StudentList {
        Student head,tail;
        Student find(String id){for(Student s=head;s!=null;s=s.next)if(s.id.equalsIgnoreCase(id))return s;return null;}
        void add(Student s){if(head==null)head=tail=s;else{tail.next=s;tail=s;}}
        String nextId(){
            int max=1000;
            for(Student s=head;s!=null;s=s.next)if(s.id.matches("S\\d+"))try{max=Math.max(max,Integer.parseInt(s.id.substring(1)));}catch(NumberFormatException ignored){}
            return "S"+(max+1);
        }
        void display(){
            if(head==null){System.out.println("No students.");return;}
            System.out.printf("%-10s %-28s %-35s%n","Member ID","Name");
            for(Student s=head;s!=null;s=s.next)System.out.printf("%-10s %-28s %-35s%n",s.id,clip(s.name,28));
        }
    }

    public static void clearConsole(String mssge){
        for(int x = 0; x < 60; x++){
            System.out.println();
        }
        System.out.println(mssge);
    }
    
    private static class BorrowRecord {
        String studentId,bookId; LocalDate borrowed; BorrowRecord next;
        BorrowRecord(String studentId,String bookId,LocalDate borrowed){this.studentId=studentId;this.bookId=bookId;this.borrowed=borrowed;}
    }
    private static class BorrowList {
        BorrowRecord head,tail;
        BorrowRecord find(String s,String b){for(BorrowRecord l=head;l!=null;l=l.next)if(l.studentId.equalsIgnoreCase(s)&&l.bookId.equalsIgnoreCase(b))return l;return null;}
        boolean has(String s,String b){return find(s,b)!=null;}
        int count(String s){int n=0;for(BorrowRecord l=head;l!=null;l=l.next)if(l.studentId.equalsIgnoreCase(s))n++;return n;}
        void add(BorrowRecord l){if(head==null)head=tail=l;else{tail.next=l;tail=l;}}
        void remove(String s,String b){
            BorrowRecord prev=null,cur=head;
            while(cur!=null&&!(cur.studentId.equalsIgnoreCase(s)&&cur.bookId.equalsIgnoreCase(b))){prev=cur;cur=cur.next;}
            if(cur==null)return;if(prev==null)head=cur.next;else prev.next=cur.next;if(tail==cur)tail=prev;
        }
    }

    private static class Request {
        String studentId,bookId; LocalDate date; Request next;
        Request(String studentId,String bookId,LocalDate date){this.studentId=studentId;this.bookId=bookId;this.date=date;}
    }
    private static class BorrowQueue {
        Request front,rear;
        boolean isEmpty(){return front==null;}
        void enqueue(Request r){r.next=null;if(rear==null)front=rear=r;else{rear.next=r;rear=r;}}
        Request dequeue(){if(front==null)return null;Request r=front;front=front.next;if(front==null)rear=null;r.next=null;return r;}
        Request peek(){return front;}
        boolean contains(String s,String b){for(Request r=front;r!=null;r=r.next)if(r.studentId.equalsIgnoreCase(s)&&r.bookId.equalsIgnoreCase(b))return true;return false;}
        boolean containsBook(String b){for(Request r=front;r!=null;r=r.next)if(r.bookId.equalsIgnoreCase(b))return true;return false;}
        int position(String s,String b){int i=1;for(Request r=front;r!=null;r=r.next,i++)if(r.studentId.equalsIgnoreCase(s)&&r.bookId.equalsIgnoreCase(b))return i;return -1;}
        Request removeFirstForBook(String id){
            Request prev=null,cur=front;
            while(cur!=null&&!cur.bookId.equalsIgnoreCase(id)){prev=cur;cur=cur.next;}
            if(cur==null)return null;
            if(prev==null)front=cur.next;else prev.next=cur.next;
            if(rear==cur)rear=prev;cur.next=null;return cur;
        }
        void showForStudent(String id){
            boolean found=false;int pos=1;
            for(Request r=front;r!=null;r=r.next,pos++)if(r.studentId.equalsIgnoreCase(id)){
                System.out.println("Position "+pos+" | Book "+r.bookId+" | Requested "+r.date);found=true;
            }
            if(!found)System.out.println("You have no waiting requests.");
        }
        void display(){
            if(front==null){System.out.println("Waiting queue is empty.");return;}
            System.out.println("WAITING QUEUE (front to rear)");
            int i=1;
            for(Request r=front;r!=null;r=r.next,i++){
                Student s=students.find(r.studentId);Book b=books.find(r.bookId);
                System.out.println(i+". "+r.studentId+" "+(s==null?"":s.name)+" | "+r.bookId+" "+(b==null?"":b.title)+" | "+r.date);
            }
        }
    }
}
