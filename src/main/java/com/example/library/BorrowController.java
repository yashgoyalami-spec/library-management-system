package com.example.library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BorrowController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @PostMapping("/borrow")
    public BorrowRecord borrowBook(@RequestBody Map<String, Long> request) {
        Long bookId = request.get("bookId");
        Long memberId = request.get("memberId");

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("No available copies of \"" + book.getTitle() + "\" right now");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        BorrowRecord record = new BorrowRecord();
        record.setBook(book);
        record.setMember(member);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(14));
        record.setStatus(BorrowStatus.BORROWED);

        return borrowRecordRepository.save(record);
    }

    @PostMapping("/return/{borrowRecordId}")
    public BorrowRecord returnBook(@PathVariable Long borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found with id: " + borrowRecordId));

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BookNotAvailableException("This book has already been returned");
        }

        record.setReturnDate(LocalDate.now());
        record.setStatus(BorrowStatus.RETURNED);

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return borrowRecordRepository.save(record);
    }

    @GetMapping("/borrow-records")
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordRepository.findAll();
    }

    @GetMapping("/borrow-records/member/{memberId}")
    public List<BorrowRecord> getBorrowRecordsByMember(@PathVariable Long memberId) {
        return borrowRecordRepository.findByMemberId(memberId);
    }
}