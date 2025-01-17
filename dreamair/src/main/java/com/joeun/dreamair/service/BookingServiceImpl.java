package com.joeun.dreamair.service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.joeun.dreamair.dto.Booking;
import com.joeun.dreamair.dto.QR;
import com.joeun.dreamair.mapper.BookingMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.joeun.dreamair.dto.Booking;
import com.joeun.dreamair.dto.Users;
import com.joeun.dreamair.mapper.BookingMapper; 

import lombok.extern.slf4j.Slf4j;
 
@Slf4j
@Service 
public class BookingServiceImpl implements BookingService{

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private QRService qrService;

    @Override
    // 가는편 항공권 조회
    public List<Booking> golist(Booking booking) throws Exception {
        log.info("서비스임플 가는편 도착지 : " + booking.getDestination());
        log.info("시버스임플 가는편 출발날짜 : " + booking.getDepartureDate());

        List<Booking> bookingList = bookingMapper.golist(booking);

        return bookingList;
    }

    @Override
    // 오는편 항공권 조회
    public List<Booking> comelist(Booking booking) throws Exception {
        log.info("서비스임플 오는편 도착지 : " + booking.getDestination());
        log.info("서비스임플 오는편 출발날짜 : " + booking.getDepartureDate());

        List<Booking> bookingList = bookingMapper.comelist(booking);

        return bookingList;
    }


    @Override
    // 탑승객들 정보 입력
    public int infoPassngers(Booking booking) throws Exception {
        log.info("서비스임플 이메일 : " + booking.getEmails()[0]);
        log.info("서비스임플 인원수 : " + booking.getPasCount());
        int result = 0;
         
        for (int i = 0; i < booking.getPasCount(); i++) {
            Booking bookingItem = new Booking();
            bookingItem.setProductNoDep(booking.getProductNoDeps()[i]);
            bookingItem.setRouteNoDep(booking.getRouteNoDeps()[i]);
            bookingItem.setPassengerName(booking.getPassengerNames()[i]);
            bookingItem.setFirstName(booking.getFirstNames()[i]);
            bookingItem.setLastName(booking.getLastNames()[i]);
            bookingItem.setGender(booking.getGenders()[i]);
            bookingItem.setBirth(booking.getBirths()[i]);
            bookingItem.setPinType(booking.getPinTypes()[i]);
            bookingItem.setPhone(booking.getPhones()[i]);
            bookingItem.setEmail(booking.getEmails()[i]);
            bookingItem.setUserPw(booking.getUserPw());

            if ( booking.getRoundTrip().equals("왕복")) {
                bookingItem.setProductNoDes(booking.getProductNoDess()[i]);
                bookingItem.setRouteNoDes(booking.getRouteNoDess()[i]);
            }

            bookingMapper.infoPassngers(bookingItem);
            result++;
        }

        log.info("왕복 등록결과 : " + result);
        
        return result;
    }

    // 회원 - 가장 최근 예매 번호 조회
    @Override
    public int latest_user_bookingNo(int userNo) throws Exception {
        int result = bookingMapper.latest_user_bookingNo(userNo);
        return result;
    }

    // 비회원 - 가장 최근 예매 번호 조회
    @Override
    public int latest_user2_bookingNo(int userNo2) throws Exception {
        int result = bookingMapper.latest_user2_bookingNo(userNo2);
        return result;
    }
    
  // 탑승권 번호 발행 + QR 코드 발행
  @Override
  public int createTicket(Booking booking, Principal principal) throws Exception {
    String userId = "";
    int result = 0;
    int bookingNo = 0;
    int ticketNo = 0;
    int count1 = 0;
    int count2 = 0;
    log.info("createTicket : " + booking);
    // ✅ TODO : 조건 pasCount 에 따라서 티켓 발행 
    for (int i = 0; i < booking.getPasCount(); i++) {
        int bookingNum = (principal == null) ? booking.getBookingNo2() : booking.getBookingNo();
        booking.setName(booking.getNames()[i]);
        booking.setPassengerNo(booking.getPassengerNos()[i]);

        Booking gobooking = bookingMapper.goTickeData(booking);
        gobooking.setUserId(principal == null ? "GUEST" : principal.getName());
        gobooking.setBoarding("0");
        gobooking.setRouteNo(booking.getRouteNoDep());
        gobooking.setSeatNo(booking.getSeatNoDepss()[i]);
        
        if( principal == null ) {
            gobooking.setBookingNo2(bookingNum);
        } else {
            gobooking.setBookingNo(bookingNum);
        }
        
        count1 = bookingMapper.createTicket(gobooking);
       
        if(booking.getRoundTrip().equals("왕복")) {
            Booking comeBooking = bookingMapper.comeTicketData(booking);
            comeBooking.setUserId(principal == null ? "GUEST" : principal.getName());
            comeBooking.setBoarding("0");
            comeBooking.setRouteNo(booking.getRouteNoDes());
            comeBooking.setSeatNo(booking.getSeatNoDesss()[i]);

            if( principal == null ) {
                comeBooking.setBookingNo2(bookingNum);
            } else {
                comeBooking.setBookingNo(bookingNum);
            }

            count2 = bookingMapper.createTicket(comeBooking);
        }
       
        int count = count1 + count2;
        // 조건 : 회원 비회원
        // 회원
        if( !userId.contains("GUEST") ) {
            bookingNo = bookingMapper.latest_user_bookingNo(booking.getUserNo());

            List<Booking> ticketList = bookingMapper.ticketList_bookingNo(bookingNo);
                for(int j = 0; j < ticketList.size(); j++){
                    Booking ticket = new Booking();
                    ticket = ticketList.get(i);
                    ticketNo = ticket.getTicketNo();
                }
        }
        else {
            bookingNo = bookingMapper.latest_user2_bookingNo(booking.getUserNo2());
            List<Booking> ticketList = bookingMapper.ticketList_bookingNo(bookingNo);
                for(int j = 0; j < ticketList.size(); j++){
                    Booking ticket = new Booking();
                    ticket = ticketList.get(i);
                    ticketNo = ticket.getTicketNo();
                }
        }
        
        QR qr = new QR();
        qr.setParentTable("booking");
        qr.setParentNo(ticketNo);
        String url = "http://localhost:" + serverPort + "/admin/Final_check?ticketNo=" + ticketNo;
        qr.setUrl( url );
        qr.setName("QR_" + ticketNo);

        qrService.makeQR(qr);

        result += count;
    }

    return result;
  }

    // seat 테이블 좌석 상태 조회
    @Override
    public List<Booking> selectSeatStatus(int flightNo) throws Exception {
        
        List<Booking> seatList = bookingMapper.selectSeatStatus(flightNo);


        return  seatList;
    }



    // 탑승권 리스트 조회 - 회원
    @Override
    public List<Booking> selectBookingListByUser(String userId) throws Exception {

        List<Booking> bookingList = bookingMapper.selectBookingListByUser(userId);

        return bookingList;

    }


    // 탑승권 상세 조회
    @Override
    public List<Booking> selectTicket(int bookingNo) throws Exception {

        List<Booking> viewTicket = bookingMapper.selectTicket(bookingNo);

        return viewTicket;

    }

    // 출발지 조회
    @Override
    public String selectDeparture(int productNoDeps) {

        String departure = bookingMapper.selectDeparture(productNoDeps);

        return departure;
    }

    // 도착지 조회
    @Override
    public String selectDestination(int productNoDeps) {

        String destination = bookingMapper.selectDestination(productNoDeps);

        return destination;
    }

    // 출발지명과 도착지명으로 노선 번호 조회
    @Override
    public int selectRouteNo(String departure, String destination) {
        
        int selectRouteNo = bookingMapper.selectRouteNo(departure, destination);

        return selectRouteNo;
    }

    // 탑승객 수만큼 info 테이블의 passenger_no 조회
    @Override
    public List<String> selectLastPasNos(int pasCount) {

        List<String> selectLastPasNos = bookingMapper.selectLastPasNos(pasCount);

        return selectLastPasNos;
    }

    // @Override
    // 여권 정보 입력
    // public int infoPassport(Users user) throws Exception {
    //     log.info("여권번호 : " + user.getPassportNos()[0]);
    //     log.info("라스트네임 : " + user.getLastNames()[0]);
    //     log.info("여권만료일자 : " + user.getExpirationDates()[0]);
    //     log.info("여권번호 : " + user.getUserId());

    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
    //     int result = 0;
        
    //     for (int i = 0; i < user.getPassportNos().length; i++) {
    //         Users userItem = new Users();
    //         userItem.setPassportNo(user.getPassportNos()[i]);
    //         userItem.setPinType(user.getPinTypes()[i]);
    //         userItem.setLastName(user.getLastNames()[i]);
    //         userItem.setFirstName(user.getFirstNames()[i]);
    //         userItem.setNationality(user.getNationalitys()[i]);
    //         userItem.setExpirationDate(user.getExpirationDates()[i]);
            
    //         if(authentication.isAuthenticated()) {
    //             userItem.setUserId(user.getUserId()); 
    //         } 

    //         bookingMapper.infoPassport(userItem);
    //         result++;
    //     }

    //     return result;
    // }


    @Override
    // 편도 항공 스케줄(탑승객 유의사항 안내)
    public List<Booking> goScheduleList(Booking booking) throws Exception {
        log.info("탑승객 이름 배열 서비스: " + booking.getPassengerNames()[0]);
        log.info("탑승객 인원: " + booking.getPasCount());
        log.info("탑승객 번호: " + booking.getPhones()[0]);
        List<Booking> bookingList = new ArrayList<Booking>();

        for (int i = 0; i < booking.getPasCount(); i++) {
            Booking bookingItem = new Booking();
            bookingItem.setPassengerName(booking.getPassengerNames()[i]);
            bookingItem.setPhone(booking.getPhones()[i]);
            int passengerNo = bookingMapper.getPasNo(bookingItem);
            bookingItem.setPassengerNo(passengerNo); 

            bookingItem = bookingMapper.goScheduleList(bookingItem);
            if ( booking.getPayment() != null && booking.getPayment().equals("확인") ) { 
                bookingItem.setSeatNoDep(booking.getSeatNoDepss()[i]);   // 페이먼트
            } else {
                bookingItem.setSeatNoDep(booking.getSeatNoDeps().get(i));   // 노티스
            }

            bookingList.add(bookingItem);
        }
        
        return bookingList;
    }
    
    @Override
    // 왕복 항공 스케줄(탑승객 유의사항 안내)
    public List<Booking> comeScheduleList(Booking booking) throws Exception {
         log.info("왕복 탑승객 이름 배열 서비스: " + booking.getPassengerNames()[0]);
        log.info("왕복 탑승객 인원: " + booking.getPasCount());
        log.info("왕복 탑승객 번호: " + booking.getPhones()[0]);
        List<Booking> bookingList = new ArrayList<Booking>();

        for (int i = 0; i < booking.getPasCount(); i++) {
            Booking bookingItem = new Booking();
            bookingItem.setPassengerName(booking.getPassengerNames()[i]);
            bookingItem.setPhone(booking.getPhones()[i]);
            int passengerNo = bookingMapper.getPasNo(bookingItem);
            bookingItem.setPassengerNo(passengerNo);

            bookingItem = bookingMapper.comeScheduleList(bookingItem);
             if ( booking.getPayment() != null && booking.getPayment().equals("확인") ) { 
                bookingItem.setSeatNoDes(booking.getSeatNoDesss()[i]);   // 페이먼트
            } else {
                bookingItem.setSeatNoDes(booking.getSeatNoDess().get(i));   // 노티스
            }

            bookingList.add(bookingItem);
        }

        return bookingList;
    }

    @Override
    // 예매 테이블 등록
    public int bookingInsert(Booking booking, Principal principal) throws Exception {
        int result = 0;
        int result1 = 0;
        int result2 = 0;
        int tmp = 0;
        for (int i = 0; i < booking.getPasCount(); i++) {
            Booking bookingItem = new Booking();
            String loginId = principal != null ? principal.getName() : "GUEST";
            bookingItem.setName(booking.getNames()[i]);
            bookingItem.setPassengerNo(booking.getPassengerNos()[i]);
            bookingItem.setSeatNoDep(booking.getSeatNoDepss()[i]);
            tmp = bookingMapper.goInsertSeat(bookingItem);

            if (loginId.equals("GUEST")) {
                bookingItem.setUserNo2(booking.getUserNo2());
                log.info("비회원넘버if : " + booking.getUserNo2());
            } else {
                bookingItem.setUserNo(booking.getUserNo());
                log.info("회원넘버if : " + booking.getUserNo());
            }
            
            bookingItem.setPasCount(booking.getPasCount());
            bookingItem.setRoundTrip(booking.getRoundTrip());
            bookingItem.setStatus(booking.getStatus());
            bookingItem.setProductNoDep(booking.getProductNoDep());
            bookingItem.setProductIdDep(booking.getProductIdDeps()[0]);
            bookingItem.setRouteNoDep(booking.getRouteNoDep());
            log.info("가는편 상품 아이디 : " + bookingItem.getProductIdDep());
            
            if (booking.getRoundTrip().equals("왕복")) {
                bookingItem.setSeatNoDes(booking.getSeatNoDesss()[i]);
                tmp = bookingMapper.comeInsertSeat(bookingItem);
                bookingItem.setProductNoDes(booking.getProductNoDes());
                bookingItem.setProductIdDes(booking.getProductIdDess()[0]);
                bookingItem.setRouteNoDes(booking.getRouteNoDes());
                log.info("오는편 상품 번호 : " + booking.getProductNoDes());
                log.info("오는편 상품 아이디 : " + bookingItem.getProductIdDes());
                result2 = bookingMapper.comeBookingInsert(bookingItem);
            }
                result1 = bookingMapper.goBookingInsert(bookingItem);
        }
                // int no = booking.getNo();
                // booking.setBookingNo("AIRBT00000000" + no);
        result = result1 + result2;

        return result;
    }

   
     // 예매 번호로 탑승권 정보(번호) 조회
    @Override
    public List<Booking> ticketList_bookingNo(int bookingNo) throws Exception {
        List<Booking> ticketList_bookingNo = bookingMapper.ticketList_bookingNo(bookingNo);
        return ticketList_bookingNo;
    }

    @Override
    public int selectRouteNoByDes(String destination) {

        int selectRouteNoByDes = bookingMapper.selectRouteNoByDes(destination);

        return selectRouteNoByDes;
    }

    @Override
    public List<Booking> bookedSeatStatus(int flightNo) throws Exception {
        List<Booking> bookedSeatStatus = bookingMapper.bookedSeatStatus(flightNo);
        return bookedSeatStatus; 
    }

    @Override
    public int selectLastBookingNo(int bookingNo) throws Exception {
        
        int result = bookingMapper.selectLastBookingNo(bookingNo);
        return result;
    }




}
