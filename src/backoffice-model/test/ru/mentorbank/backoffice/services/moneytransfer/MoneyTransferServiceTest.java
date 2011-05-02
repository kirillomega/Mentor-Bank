package ru.mentorbank.backoffice.services.moneytransfer;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.ExpectedException;

import ru.mentorbank.backoffice.dao.exception.OperationDaoException;
import ru.mentorbank.backoffice.model.Operation;
import ru.mentorbank.backoffice.dao.OperationDao;
import ru.mentorbank.backoffice.model.stoplist.JuridicalStopListRequest;
import ru.mentorbank.backoffice.model.transfer.AccountInfo;
import ru.mentorbank.backoffice.model.transfer.PhysicalAccountInfo;
import ru.mentorbank.backoffice.model.transfer.JuridicalAccountInfo;
import ru.mentorbank.backoffice.model.transfer.TransferRequest;
import ru.mentorbank.backoffice.services.accounts.AccountService;
import ru.mentorbank.backoffice.services.accounts.AccountServiceBean;
import ru.mentorbank.backoffice.services.moneytransfer.exceptions.TransferException;
import ru.mentorbank.backoffice.services.stoplist.StopListServiceStub;
import ru.mentorbank.backoffice.services.stoplist.StopListService;
import ru.mentorbank.backoffice.test.AbstractSpringTest;

public class MoneyTransferServiceTest extends AbstractSpringTest {

	@Autowired
	private MoneyTransferServiceBean moneyTransferService;
	private AccountService mockedAccountService;
	private StopListService mockedStopListService;
	private JuridicalAccountInfo srcAccountInfo;
	private TransferRequest transferRequest;
	private OperationDao mockedOperationDao;
	private JuridicalAccountInfo dstAccountInfo;

	@Before
	public void setUp() {
		
		mockedAccountService = mock(AccountServiceBean.class);
		when(mockedAccountService.verifyBalance(srcAccountInfo)).thenReturn(
				true);
		mockedStopListService = mock(StopListServiceStub.class);
		mockedOperationDao = mock(OperationDao.class);
		dstAccountInfo = new JuridicalAccountInfo();
		dstAccountInfo.setAccountNumber("111111111111111");
		dstAccountInfo.setInn(StopListServiceStub.INN_FOR_OK_STATUS);
		srcAccountInfo = new JuridicalAccountInfo();
		srcAccountInfo.setAccountNumber("55555555555555");
		srcAccountInfo.setInn(StopListServiceStub.INN_FOR_OK_STATUS);
		
		transferRequest = new TransferRequest();
		transferRequest.setSrcAccount(srcAccountInfo);
		transferRequest.setDstAccount(dstAccountInfo);
		// Dynamic Stub
		
		moneyTransferService.setAccountService(mockedAccountService);
		moneyTransferService.setStopListService(mockedStopListService);
		moneyTransferService.setOperationDao(mockedOperationDao);
	}

	@Test
	@ExpectedException(TransferException.class)
	public void transfer() throws TransferException, OperationDaoException {
				
		moneyTransferService.transfer(transferRequest);
		verify(mockedAccountService).verifyBalance(srcAccountInfo);
		
		//проверяем, что вызывался метод сервиса стоп листов для юрика и с чем
        ArgumentCaptor<JuridicalStopListRequest> juridicalStopListRequest = ArgumentCaptor.forClass(JuridicalStopListRequest.class);
		verify(mockedStopListService).getJuridicalStopListInfo(juridicalStopListRequest.capture());
		
		verify(mockedOperationDao).saveOperation(ArgumentCaptor.forClass(Operation.class).capture());
		
	}
}
