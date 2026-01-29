package com.example.zapas.domain.usecase


abstract class UseCase<in Params, out T> {
    abstract suspend operator fun invoke(params: Params): T
}


abstract class NoParamsUseCase<out T> {
    abstract suspend operator fun invoke(): T
}