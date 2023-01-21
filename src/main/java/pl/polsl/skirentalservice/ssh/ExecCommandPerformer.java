/*
 * Copyright (c) 2023 by multiple authors
 * Silesian University of Technology
 *
 *  File name: ExecCommandPerformer.java
 *  Last modified: 21/01/2023, 07:58
 *  Project name: ski-rental-service
 *
 * This project was written for the purpose of a subject taken in the study of Computer Science.
 * This project is not commercial in any way and does not represent a viable business model
 * of the application. Project created for educational purposes only.
 */

package pl.polsl.skirentalservice.ssh;

import org.slf4j.*;
import java.util.*;

import pl.polsl.skirentalservice.core.ssh.*;

import static pl.polsl.skirentalservice.ssh.Command.*;
import static pl.polsl.skirentalservice.ssh.ReturnCode.isInvalid;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class ExecCommandPerformer implements IExecCommandPerformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecCommandPerformer.class);
    private final SshSocketBean sshSocket;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ExecCommandPerformer(SshSocketBean sshSocket) {
        this.sshSocket = sshSocket;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void createMailbox(String email, String password) throws CommandPerformException {
        final Map<String, String> entries = Map.of("email", email, "password", password);
        final var createResult = sshSocket.executeCommand(CREATE_MAILBOX, entries, BaseCommandResponse.class);
        final var capacityResult = sshSocket.executeCommand(SET_MAILBOX_CAPACITY, Map.of("email", email),
            BaseCommandResponse.class);
        if (isInvalid(createResult)) {
            throw new CommandPerformException("Nieudane utworzenie skrzynki pocztowej pracownika.", createResult.getMsg());
        }
        if (isInvalid(capacityResult)) {
            throw new CommandPerformException("Nieudane ustawienie limitu powierzchni skrzynki pocztowej pracownika.",
                capacityResult.getMsg());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void updateMailboxPassword(String email, String newPassword) throws CommandPerformException {
        final Map<String, String> entries = Map.of("email", email, "newPassword", newPassword);
        final var response = sshSocket.executeCommand(UPDATE_MAILBOX_PASSWORD, entries, BaseCommandResponse.class);
        if (isInvalid(response)) {
            throw new CommandPerformException("Nieudane zaktualizowanie hasła skrzynki pocztowej.", response.getMsg());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void deleteMailbox(String email) throws CommandPerformException {
        final var response = sshSocket.executeCommand(DELETE_MAILBOX, Map.of("email", email), BaseCommandResponse.class);
        if (isInvalid(response)) {
            throw new CommandPerformException("Nieudane usunięcie skrzynki pocztowej.", response.getMsg());
        }
    }
}